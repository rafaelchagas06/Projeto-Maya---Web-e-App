package com.example.rpgclinicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rpgclinicapp.models.Exercicio;
import java.util.List;

public class ExercicioAdapter extends RecyclerView.Adapter<ExercicioAdapter.ViewHolder> {

    private List<Exercicio> lista;
    private OnExercicioClickListener listener;

    // Interface para detetar o clique no botão "Marcar Completo"
    public interface OnExercicioClickListener {
        void onMarcarCompleto(View view, Exercicio exercicio);
    }

    public ExercicioAdapter(List<Exercicio> lista, OnExercicioClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercicio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercicio ex = lista.get(position);
        holder.tvNome.setText(ex.getNome());
        holder.tvRepeticoes.setText(ex.getRepeticoes());
        holder.tvDescricao.setText(ex.getDescricao());

        // Configura o clique no botão de completar
        holder.btnCompleto.setOnClickListener(v -> {
            if (listener != null) listener.onMarcarCompleto(v, ex);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvRepeticoes, tvDescricao, btnCompleto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_item_nome);
            tvRepeticoes = itemView.findViewById(R.id.tv_item_repeticoes);
            tvDescricao = itemView.findViewById(R.id.tv_item_descricao);
            btnCompleto = itemView.findViewById(R.id.btn_item_completo);
        }
    }
}