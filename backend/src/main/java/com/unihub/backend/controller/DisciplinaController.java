package com.unihub.backend.controller;

import com.unihub.backend.model.Disciplina;
import com.unihub.backend.model.HorarioAula;
import com.unihub.backend.service.DisciplinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import com.unihub.backend.model.Ausencia;

@RestController
@RequestMapping({"/disciplinas", "/api/disciplinas"})
@CrossOrigin(origins = "*")
public class DisciplinaController {

    @Autowired
    private DisciplinaService service;

    @GetMapping
    public List<Disciplina> listarTodas(@AuthenticationPrincipal Long usuarioId) {
        return service.listarTodas(usuarioId);
    }

    @GetMapping("/{id}")
      public Disciplina buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        return service.buscarPorId(id, usuarioId);
    }

    @PostMapping
    public Disciplina criar(@RequestBody Disciplina disciplina, @AuthenticationPrincipal Long usuarioId) {
        return service.salvar(disciplina, usuarioId);
    }

    @PutMapping("/{id}")
    public Disciplina atualizar(@PathVariable Long id, @RequestBody Disciplina novaDisciplina, @AuthenticationPrincipal Long usuarioId) {
        Disciplina existente = service.buscarPorId(id, usuarioId);

        existente.setCodigo(novaDisciplina.getCodigo());
        existente.setNome(novaDisciplina.getNome());
        existente.setProfessor(novaDisciplina.getProfessor());
        existente.setPeriodo(novaDisciplina.getPeriodo());
        existente.setCargaHoraria(novaDisciplina.getCargaHoraria());
        existente.setQtdSemanas(novaDisciplina.getQtdSemanas());
        existente.setDataInicioSemestre(novaDisciplina.getDataInicioSemestre());
        existente.setDataFimSemestre(novaDisciplina.getDataFimSemestre());
        existente.setEmailProfessor(novaDisciplina.getEmailProfessor());
        existente.setPlataforma(novaDisciplina.getPlataforma());
        existente.setTelefoneProfessor(novaDisciplina.getTelefoneProfessor());
        existente.setSalaProfessor(novaDisciplina.getSalaProfessor());
        existente.setAtiva(novaDisciplina.isAtiva());
        existente.setReceberNotificacoes(novaDisciplina.isReceberNotificacoes());
        existente.setAusenciasPermitidas(novaDisciplina.getAusenciasPermitidas());
    
        existente.getAulas().clear();
        if (novaDisciplina.getAulas() != null) {
            for (HorarioAula aula : novaDisciplina.getAulas()) {
                aula.setDisciplina(existente);
                existente.getAulas().add(aula);
            }
        }

        return service.salvar(existente, usuarioId);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Long usuarioId) {
        service.excluir(id, usuarioId);
    }

    @GetMapping("/pesquisa")
    public List<Disciplina> buscarPorNome(@RequestParam String nome, @AuthenticationPrincipal Long usuarioId) {
        return service.buscarPorNome(nome, usuarioId);
    }

}
