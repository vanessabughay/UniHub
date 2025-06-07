package com.example.unihub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import android.content.Intent
import android.widget.ImageButton


class DisciplinasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DisciplinaAdapter
    private lateinit var searchView: SearchView

    private val todasDisciplinas = listOf(
        Disciplina("DS436", "Disciplina 1", "Terça-feira", "A13", "19:00 - 22:00", "2025/1", "Ativa"),
        Disciplina("DS437", "Disciplina 2", "Quarta-feira", "C02", "19:00 - 22:00", "2025/1", "Ativa"),
        Disciplina("DS438", "Disciplina 3", "Quinta-feira", "A09", "19:00 - 22:00", "2025/1", "Ativa"),
        Disciplina("DS439", "Disciplina 4", "Sexta-feira", "A15", "19:00 - 22:00", "2025/1", "Ativa", selecionada = true)
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

        val addButton = findViewById<ImageButton>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, ManterInfoDisciplina::class.java)
            startActivity(intent)
        }

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
