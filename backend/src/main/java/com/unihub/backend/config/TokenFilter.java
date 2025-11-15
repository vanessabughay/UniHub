package com.unihub.backend.config;

import com.unihub.backend.service.AutenticacaoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

@Component
public class TokenFilter extends OncePerRequestFilter {

    @Autowired
    private AutenticacaoService autenticacaoService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    // LOG 1 – sempre imprime o header
    System.out.println("DEBUG TOKENFILTER: " + request.getMethod() + " " + request.getRequestURI()
            + " | header=" + header);

    if (header != null && header.startsWith("Bearer ")) {
        String token = header.substring(7);

        // chama o serviço que valida/busca o usuário
        Long usuarioId = autenticacaoService.getUsuarioIdPorToken(token);

        // LOG 2 – mostra o token e o usuário resolvido (ou null)
        System.out.println("DEBUG TOKENFILTER: token=" + token + ", usuarioId=" + usuarioId);

        if (usuarioId != null) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            usuarioId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    filterChain.doFilter(request, response);
}

}