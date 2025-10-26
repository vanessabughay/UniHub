package com.unihub.backend.controller;

import com.unihub.backend.model.Avaliacao;
import com.unihub.backend.model.Disciplina;
import com.unihub.backend.model.HorarioAula;
import com.unihub.backend.service.DisciplinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disciplinas")
@CrossOrigin(origins = "*")
public class DisciplinaController {

    @Autowired
    private DisciplinaService service;

    @GetMapping
    public List<Disciplina> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Disciplina buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public Disciplina criar(@RequestBody Disciplina disciplina) {
        return service.salvar(disciplina);
    }

    @PutMapping("/{id}")
    public Disciplina atualizar(@PathVariable Long id, @RequestBody Disciplina novaDisciplina) {
        Disciplina existente = service.buscarPorId(id);

        existente.setNome(novaDisciplina.getNome());
        existente.setProfessor(novaDisciplina.getProfessor());
        existente.setPeriodo(novaDisciplina.getPeriodo());
        existente.setCargaHoraria(novaDisciplina.getCargaHoraria());
        existente.setDataInicioSemestre(novaDisciplina.getDataInicioSemestre());
        existente.setDataFimSemestre(novaDisciplina.getDataFimSemestre());
        existente.setEmailProfessor(novaDisciplina.getEmailProfessor());
        existente.setPlataforma(novaDisciplina.getPlataforma());
        existente.setTelefoneProfessor(novaDisciplina.getTelefoneProfessor());
        existente.setSalaProfessor(novaDisciplina.getSalaProfessor());
        existente.setAtiva(novaDisciplina.isAtiva());
        existente.setReceberNotificacoes(novaDisciplina.isReceberNotificacoes());

        if (novaDisciplina.getProprietario() != null) {
            existente.setProprietario(novaDisciplina.getProprietario());
        }


        if (existente.getAulas() != null) {
            existente.getAulas().clear();
        } else {
            existente.setAulas(new java.util.ArrayList<>());
        }
        if (novaDisciplina.getAulas() != null) {
            for (HorarioAula aula : novaDisciplina.getAulas()) {
                aula.setDisciplina(existente);
                existente.getAulas().add(aula);
            }
        }

        if (existente.getAvaliacoes() != null) {
            existente.getAvaliacoes().clear();
        } else {
            existente.setAvaliacoes(new java.util.ArrayList<>());
        }
        if (novaDisciplina.getAvaliacoes() != null) {
            for (Avaliacao avaliacao : novaDisciplina.getAvaliacoes()) {
                avaliacao.setDisciplina(existente);
                existente.getAvaliacoes().add(avaliacao);
            }
        }

        return service.salvar(existente);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/pesquisa")
    public List<Disciplina> buscarPorNome(@RequestParam String nome) {
    return service.buscarPorNome(nome);
}

}
