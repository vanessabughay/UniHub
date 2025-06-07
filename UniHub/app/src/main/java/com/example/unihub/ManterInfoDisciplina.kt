package com.example.unihub

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

class ManterInfoDisciplina : AppCompatActivity() {

    private lateinit var editCodigo: EditText
    private lateinit var editNome: EditText
    private lateinit var editProfessor: EditText
    private lateinit var spinnerPeriodo: Spinner
    private lateinit var editCH: EditText
    private lateinit var editAulasSemana: EditText
    private lateinit var containerSubcards: LinearLayout
    private lateinit var editDataInicio: EditText
    private lateinit var editDataFim: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPlataformas: EditText
    private lateinit var editTelefone: EditText
    private lateinit var editSala: EditText
    private lateinit var spinnerEstado: Spinner
    private lateinit var checkNotificacoes: CheckBox
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluir: Button

    private val diasDaSemana = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manter_info_disciplina)

        inicializarComponentes()
        configurarSpinners()
        configurarEventos()
    }

    private fun inicializarComponentes() {
        editCodigo = findViewById(R.id.editCodigo)
        editNome = findViewById(R.id.editNome)
        editProfessor = findViewById(R.id.editProfessor)
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo)
        editCH = findViewById(R.id.editCH)
        editAulasSemana = findViewById(R.id.editAulasSemana)
        containerSubcards = findViewById(R.id.containerSubcards)
        editDataInicio = findViewById(R.id.editDataInicio)
        editDataFim = findViewById(R.id.editDataFim)
        editEmail = findViewById(R.id.editEmail)
        editPlataformas = findViewById(R.id.editPlataformas)
        editTelefone = findViewById(R.id.editTelefone)
        editSala = findViewById(R.id.editSala)
        spinnerEstado = findViewById(R.id.spinnerEstado)
        checkNotificacoes = findViewById(R.id.checkNotificacoes)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)
    }

    private fun configurarSpinners() {
        ArrayAdapter.createFromResource(
            this,
            R.array.periodos_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPeriodo.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.estado_disciplina_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEstado.adapter = adapter
        }
    }

    private fun configurarEventos() {
        editAulasSemana.addTextChangedListener {
            gerarSubcards()
        }

        btnSalvar.setOnClickListener {
            Toast.makeText(this, "Disciplina salva com sucesso!", Toast.LENGTH_SHORT).show()
        }

        btnExcluir.setOnClickListener {
            limparCampos()
            Toast.makeText(this, "Disciplina excluída.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gerarSubcards() {
        containerSubcards.removeAllViews()

        val quantidade = editAulasSemana.text.toString().toIntOrNull() ?: return

        if (quantidade <= 0) return

        // Linha antes do primeiro subcard
        containerSubcards.addView(criarLinhaSeparadora())

        for (i in 1..quantidade) {
            val subcard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 16)
            }

            // LINHA: Dia
            val linhaDia = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val label = TextView(this@ManterInfoDisciplina).apply {
                    text = "Dia:"
                    setPadding(0, 0, 8, 0)
                }
                val spinner = Spinner(this@ManterInfoDisciplina).apply {
                    adapter = ArrayAdapter(this@ManterInfoDisciplina, android.R.layout.simple_spinner_item, diasDaSemana).also {
                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(label)
                addView(spinner)
            }

            // LINHA: Ensalamento
            val linhaSala = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val label = TextView(this@ManterInfoDisciplina).apply {
                    text = "Ensalamento:"
                    setPadding(0, 0, 8, 0)
                }
                val input = EditText(this@ManterInfoDisciplina).apply {
                    hint = "Sala A13"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                addView(label)
                addView(input)
            }

            // LINHA: Horários
            val linhaHorario = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val labelInicio = TextView(this@ManterInfoDisciplina).apply {
                    text = "Início:"
                    setPadding(0, 0, 8, 0)
                }
                val inputInicio = EditText(this@ManterInfoDisciplina).apply {
                    hint = "19:00"
                    inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val labelFim = TextView(this@ManterInfoDisciplina).apply {
                    text = "Fim:"
                    setPadding(16, 0, 8, 0)
                }
                val inputFim = EditText(this@ManterInfoDisciplina).apply {
                    hint = "21:00"
                    inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                addView(labelInicio)
                addView(inputInicio)
                addView(labelFim)
                addView(inputFim)
            }

            // Monta o subcard e adiciona
            subcard.addView(linhaDia)
            subcard.addView(linhaSala)
            subcard.addView(linhaHorario)
            containerSubcards.addView(subcard)

            // Linha depois do subcard
            containerSubcards.addView(criarLinhaSeparadora())
        }
    }


    private fun limparCampos() {
        editCodigo.text.clear()
        editNome.text.clear()
        editProfessor.text.clear()
        spinnerPeriodo.setSelection(0)
        editCH.text.clear()
        editAulasSemana.text.clear()
        containerSubcards.removeAllViews()
        editDataInicio.text.clear()
        editDataFim.text.clear()
        editEmail.text.clear()
        editPlataformas.text.clear()
        editTelefone.text.clear()
        editSala.text.clear()
        spinnerEstado.setSelection(0)
        checkNotificacoes.isChecked = false
    }

    private fun criarLinhaSeparadora(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1 // altura da linha
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
            setBackgroundColor(getColor(android.R.color.darker_gray))
        }
    }

}
