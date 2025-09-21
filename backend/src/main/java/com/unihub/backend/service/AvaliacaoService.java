package com.unihub.backend.service;

import com.unihub.backend.model.Contato;
import com.unihub.backend.model.Avaliacao;
import com.unihub.backend.model.Disciplina;
import com.unihub.backend.repository.ContatoRepository;
import com.unihub.backend.repository.AvaliacaoRepository;
import com.unihub.backend.repository.DisciplinaRepository;

import jakarta.persistence.EntityNotFoundException; // Boa prática para exceções específicas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operações de modificação

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ContatoRepository contatoRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository; // Injetar DisciplinaRepository


    @Transactional(readOnly = true) // Boa prática para métodos de leitura
    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Avaliacao buscarPorId(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliacao não encontrado com ID: " + id));
    }

    @Transactional // Métodos que modificam dados devem ser transacionais
    public Avaliacao criarAvaliacao(Avaliacao avaliacaoRequest) {
        // Se o avaliacao vem com uma lista de membros (Contatos) que já existem (têm ID),
        // 1. Associar a Disciplina
        if (avaliacaoRequest.getDisciplina() != null && avaliacaoRequest.getDisciplina().getId() != null) {
            Long disciplinaId = avaliacaoRequest.getDisciplina().getId();
            // Aqui, estamos assumindo que o DisciplinaService.buscarPorId não precisa de usuarioId,
            // ou que você tem um método no disciplinaRepository para buscar só por ID.
            // Se o buscarPorId do DisciplinaService precisar de usuarioId, você terá que adaptar
            // ou passar o usuarioId para este método.
            // Para simplificar, vou usar disciplinaRepository.findById diretamente.
            Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                    .orElseThrow(() -> new EntityNotFoundException("Disciplina com ID " + disciplinaId + " não encontrada ao criar avaliação."));
            avaliacaoRequest.setDisciplina(disciplina); // Define a instância gerenciada
        } else {
            // Decide como lidar: erro se disciplina for obrigatória, ou permitir nulo se for opcional
            throw new IllegalArgumentException("Disciplina é obrigatória para criar uma avaliação.");
        }


        // 2. Lidar com Integrantes (como você já tinha)
        if (avaliacaoRequest.getIntegrantes() != null && !avaliacaoRequest.getIntegrantes().isEmpty()) {
            List<Contato> membrosGerenciados = new ArrayList<>();
            for (Contato membroDto : avaliacaoRequest.getIntegrantes()) {
                if (membroDto.getId() == null) {
                    throw new IllegalArgumentException("Contatos fornecidos para uma nova avaliação devem ter um ID.");
                } else {
                    Contato membroExistente = contatoRepository.findById(membroDto.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Contato com ID " + membroDto.getId() + " não encontrado."));
                    membrosGerenciados.add(membroExistente);
                }
            }
            avaliacaoRequest.getIntegrantes().clear();
            avaliacaoRequest.getIntegrantes().addAll(membrosGerenciados);
        }

        Avaliacao novaAvaliacao = avaliacaoRepository.save(avaliacaoRequest);
        return novaAvaliacao;
    }


    @Transactional
    public Avaliacao atualizarAvaliacao(Long id, Avaliacao avaliacaoDetalhesRequest) {
        Avaliacao avaliacaoExistente = buscarPorId(id); // Reusa o método que já lança exceção

        if (avaliacaoDetalhesRequest.getDescricao() != null) {
            avaliacaoExistente.setDescricao(avaliacaoDetalhesRequest.getDescricao());
        }

        if (avaliacaoDetalhesRequest.getTipoAvaliacao() != null) {
            avaliacaoExistente.setTipoAvaliacao(avaliacaoDetalhesRequest.getTipoAvaliacao());
        }

        if (avaliacaoDetalhesRequest.getDataEntrega() != null) {
            avaliacaoExistente.setDataEntrega(avaliacaoDetalhesRequest.getDataEntrega());
        }
        if (avaliacaoDetalhesRequest.getPeso() != null) {
            avaliacaoExistente.setPeso(avaliacaoDetalhesRequest.getPeso());
        }
        if (avaliacaoDetalhesRequest.getEstado() != null) {
            avaliacaoExistente.setEstado(avaliacaoDetalhesRequest.getEstado());
        }
        if (avaliacaoDetalhesRequest.getPrioridade() != null) {
            avaliacaoExistente.setPrioridade(avaliacaoDetalhesRequest.getPrioridade());
        }
        if (avaliacaoDetalhesRequest.getModalidade() != null) {
            avaliacaoExistente.setModalidade(avaliacaoDetalhesRequest.getModalidade());
        }



        avaliacaoExistente.setReceberNotificacoes(avaliacaoDetalhesRequest.getReceberNotificacoes());


        // 1. Atualizar a Disciplina associada (se permitido e fornecido)
        if (avaliacaoDetalhesRequest.getDisciplina() != null && avaliacaoDetalhesRequest.getDisciplina().getId() != null) {
            Long novaDisciplinaId = avaliacaoDetalhesRequest.getDisciplina().getId();
            Disciplina disciplinaNova = disciplinaRepository.findById(novaDisciplinaId)
                    .orElseThrow(() -> new EntityNotFoundException("Nova disciplina com ID " + novaDisciplinaId + " não encontrada."));

            // Se a disciplina mudou, e você tem uma relação bidirecional gerenciada
            // você pode querer remover a avaliação da lista da disciplina antiga.
            // Disciplina disciplinaAntiga = avaliacaoExistente.getDisciplina();
            // if (disciplinaAntiga != null && !disciplinaAntiga.getId().equals(novaDisciplinaId)) {
            //     disciplinaAntiga.removeAvaliacao(avaliacaoExistente);
            //     // disciplinaRepository.save(disciplinaAntiga); // Opcional, dependendo do cascade
            // }

            avaliacaoExistente.setDisciplina(disciplinaNova);

            // Se bidirecional:
            // disciplinaNova.addAvaliacao(avaliacaoExistente);
            // disciplinaRepository.save(disciplinaNova); // Opcional
        }


        // 2. Lógica para atualizar a lista de integrantes (como você já tinha)
        if (avaliacaoDetalhesRequest.getIntegrantes() != null) {
            List<Contato> novosMembrosGerenciados = new ArrayList<>();
            if (!avaliacaoDetalhesRequest.getIntegrantes().isEmpty()) {
                for (Contato membroDto : avaliacaoDetalhesRequest.getIntegrantes()) {
                    if (membroDto.getId() == null) {
                        throw new IllegalArgumentException("Contato fornecido para atualização não possui ID: " + membroDto.getNome());
                    }
                    Contato membroGerenciado = contatoRepository.findById(membroDto.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Contato com ID " + membroDto.getId() + " não encontrado."));
                    novosMembrosGerenciados.add(membroGerenciado);
                }
            }
            avaliacaoExistente.getIntegrantes().clear();
            avaliacaoExistente.getIntegrantes().addAll(novosMembrosGerenciados);
        }

        return avaliacaoRepository.save(avaliacaoExistente);
    }
    // Se avaliacaoDetalhesRequest.getMembros() for null, não fazemos nada,
    // preservando a lista de membros atual do avaliacaoExistente.


    @Transactional
    public void excluir(Long id) {
        Avaliacao avaliacao = buscarPorId(id); // Para garantir que existe e para possível lógica bidirecional

        // (Opcional, se relação bidirecional e você quiser gerenciar do lado da Disciplina também)
        // Disciplina disciplinaDaAvaliacao = avaliacao.getDisciplina();
        // if (disciplinaDaAvaliacao != null) {
        //     disciplinaDaAvaliacao.removeAvaliacao(avaliacao);
        //     // disciplinaRepository.save(disciplinaDaAvaliacao); // Se não tiver CascadeType.ALL / orphanRemoval
        // }

        avaliacaoRepository.delete(avaliacao); // Usar delete(entity) pode ser melhor para callbacks JPA
    }


    @Transactional(readOnly = true)
    public List<Avaliacao> buscarPorNome(String descricao) {
        return avaliacaoRepository.findByDescricaoContainingIgnoreCase(descricao);
    }

    // Método auxiliar para expor o ContatoRepository se o Controller precisar dele
    // (embora seja melhor manter a lógica de busca de contatos no service)
    // public ContatoRepository getContatoRepository() {
    //     return contatoRepository;
    // }

}
