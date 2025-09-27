package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Grupo;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.GrupoRepository;
import jakarta.persistence.EntityNotFoundException; // Boa prática para exceções específicas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operações de modificação

import java.util.ArrayList;
import java.util.List;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository; // Renomeado para clareza

    @Autowired
    private ContatoRepository contatoRepository;

    @Transactional(readOnly = true) // Boa prática para métodos de leitura
    public List<Grupo> listarTodas() {
        return grupoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Grupo buscarPorId(Long id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado com ID: " + id));
    }

    @Transactional // Métodos que modificam dados devem ser transacionais
    public Grupo criarGrupo(Grupo grupo) {
        // Se o grupo vem com uma lista de membros (Contatos) que já existem (têm ID),
        // precisamos garantir que estamos associando as instâncias gerenciadas.
        if (grupo.getMembros() != null && !grupo.getMembros().isEmpty()) {
            List<Contato> membrosGerenciados = new ArrayList<>();
            for (Contato membroDto : grupo.getMembros()) {
                if (membroDto.getId() == null) {
                    throw new IllegalArgumentException("Contatos fornecidos para um novo grupo devem ter um ID (devem existir).");
                    // Ou, se você permitir criar Contatos transitivamente (mais complexo):
                    // Contato novoContato = contatoRepository.save(membroDto);
                    // membrosGerenciados.add(novoContato);
                } else {
                    Contato membroExistente = contatoRepository.findById(membroDto.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Contato com ID " + membroDto.getId() + " não encontrado ao criar grupo."));
                    membrosGerenciados.add(membroExistente);
                }
            }
            // Substitui a lista de DTOs pela lista de entidades gerenciadas
            grupo.getMembros().clear();
            grupo.getMembros().addAll(membrosGerenciados);
        }
        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo atualizarGrupo(Long id, Grupo grupoDetalhesRequest) {
        Grupo grupoExistente = buscarPorId(id); // Reusa o método que já lança exceção

        if (grupoDetalhesRequest.getNome() != null) {
            grupoExistente.setNome(grupoDetalhesRequest.getNome());
        }

        // Lógica para atualizar a lista de membros
        if (grupoDetalhesRequest.getMembros() != null) {
            // O cliente enviou uma lista de membros (pode ser vazia, indicando que quer remover todos).
            // Precisamos transformar os Contatos da requisição (DTOs) em entidades Contato gerenciadas.
            List<Contato> novosMembrosGerenciados = new ArrayList<>();
            if (!grupoDetalhesRequest.getMembros().isEmpty()) {
                for (Contato membroDto : grupoDetalhesRequest.getMembros()) {
                    if (membroDto.getId() == null) {
                        // Decide como lidar com isso: erro, ignorar, ou tentar criar (se permitido)
                        throw new IllegalArgumentException("Contato fornecido para atualização de grupo não possui ID: " + membroDto.getNome());
                    }
                    Contato membroGerenciado = contatoRepository.findById(membroDto.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Contato com ID " + membroDto.getId() + " não encontrado para adicionar ao grupo."));
                    novosMembrosGerenciados.add(membroGerenciado);
                }
            }

            // Agora atualiza a coleção gerenciada no 'grupoExistente'
            // 1. Limpa os membros antigos da relação
            // (Para ManyToMany, isso remove as entradas da tabela de junção.
            //  Se houver orphanRemoval=true em um OneToMany, os órfãos seriam deletados do DB.)
            grupoExistente.getMembros().clear();

            // 2. Adiciona os novos membros (já gerenciados) à relação
            if (!novosMembrosGerenciados.isEmpty()) {
                grupoExistente.getMembros().addAll(novosMembrosGerenciados);
            }
        }
        // Se grupoDetalhesRequest.getMembros() for null, não fazemos nada,
        // preservando a lista de membros atual do grupoExistente.

        return grupoRepository.save(grupoExistente);
    }

    @Transactional
    public void excluir(Long id) {
        if (!grupoRepository.existsById(id)) {
            throw new EntityNotFoundException("Grupo não encontrado com ID: " + id + " para exclusão.");
        }
        grupoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Grupo> buscarPorNome(String nome) {
        return grupoRepository.findByNomeContainingIgnoreCase(nome);
    }

    // Método auxiliar para expor o ContatoRepository se o Controller precisar dele
    // (embora seja melhor manter a lógica de busca de contatos no service)
    // public ContatoRepository getContatoRepository() {
    //     return contatoRepository;
    // }
}
