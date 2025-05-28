package com.example.unihub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class DisciplinaAdapter(
    private val disciplinas: List<Disciplina>,
    private val onShareClicked: (Disciplina) -> Unit
) : RecyclerView.Adapter<DisciplinaAdapter.DisciplinaViewHolder>() {

    class DisciplinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.tituloDisciplina)
        val localHorario: TextView = view.findViewById(R.id.localHorario)
        val compartilhar: ImageButton = view.findViewById(R.id.shareButton)
        val card: CardView = view as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisciplinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_disciplina, parent, false)
        return DisciplinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DisciplinaViewHolder, position: Int) {
        val disciplina = disciplinas[position]
        val context = holder.card.context

        holder.titulo.text = "${disciplina.codigo} ${disciplina.nome}"
        holder.localHorario.text = "${disciplina.dia} - Sala ${disciplina.sala}\n${disciplina.horario}"

        val corNormal = context.getColor(R.color.card_background)
        val corSelecionada = context.getColor(R.color.card_background_selected)
        holder.card.setCardBackgroundColor(if (disciplina.selecionada) corSelecionada else corNormal)

        holder.compartilhar.setOnClickListener {
            onShareClicked(disciplina)
        }
    }

    override fun getItemCount(): Int = disciplinas.size
}
