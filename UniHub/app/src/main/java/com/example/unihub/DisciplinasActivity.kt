package com.example.unihub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView

class DisciplinasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DisciplinaAdapter
    private lateinit var searchView: SearchView

    private val todasDisciplinas = listOf(
        Disciplina("DS436", "Disciplina 1", "Terça-feira", "A13", "19:00 - 22:00"),
        Disciplina("DS437", "Disciplina 2", "Quarta-feira", "C02", "19:00 - 22:00"),
        Disciplina("DS438", "Disciplina 3", "Quinta-feira", "A09", "19:00 - 22:00"),
        Disciplina("DS439", "Disciplina 4", "Sexta-feira", "A15", "19:00 - 22:00", selecionada = true)
    )

    private var disciplinasFiltradas = todasDisciplinas.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disciplinas)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        configurarAdapter()
        configurarBusca()
    }

    private fun configurarAdapter() {
        adapter = DisciplinaAdapter(disciplinasFiltradas) { disciplina ->
            Toast.makeText(this, "Compartilhar ${disciplina.nome}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }


    private fun configurarBusca() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText.orEmpty().lowercase()
                disciplinasFiltradas = todasDisciplinas.filter {
                    it.nome.lowercase().contains(texto) || it.codigo.lowercase().contains(texto)
                }.toMutableList()
                configurarAdapter()
                return true
            }
        })
    }
}
