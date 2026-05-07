package com.example.rpgclinicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    // Método útil caso você precise atualizar a lista depois
    public void setExercicios(List<Exercicio> novosExercicios) {
        this.lista = novosExercicios;
        notifyDataSetChanged();
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

        // Verifica se os textos não são nulos antes de tentar preencher
        if (ex.getNome() != null) holder.tvNome.setText(ex.getNome());
        if (ex.getRepeticoes() != null) holder.tvRepeticoes.setText(ex.getRepeticoes());
        if (ex.getDescricao() != null) holder.tvDescricao.setText(ex.getDescricao());

        // --- INÍCIO DA LÓGICA DA IMAGEM ---
        String urlDaMidia = ex.getUrlImagem(); // Chama o método que criamos no Exercicio.java

        if (urlDaMidia != null && !urlDaMidia.isEmpty()) {
            holder.imgExercicio.setVisibility(View.VISIBLE); // Garante que a foto aparece

            // O Glide vai na internet, baixa a foto e coloca na ImageView
            Glide.with(holder.itemView.getContext())
                    .load(urlDaMidia)
                    .placeholder(android.R.color.darker_gray) // Fundo cinza provisório enquanto carrega
                    .error(android.R.color.holo_red_light)    // Fica vermelho caso o link esteja quebrado
                    .centerCrop() // Ajusta a imagem para não ficar achatada
                    .into(holder.imgExercicio);
        } else {
            // Se o exercício não tiver foto no banco de dados, esconde o espaço vazio
            holder.imgExercicio.setVisibility(View.GONE);

            // Limpa a foto anterior da memória do Android para evitar o bug de reciclagem
            Glide.with(holder.itemView.getContext()).clear(holder.imgExercicio);
            holder.imgExercicio.setImageDrawable(null);
        }
        // --- FIM DA LÓGICA DA IMAGEM ---

        // Configura o clique no botão de completar
        holder.btnCompleto.setOnClickListener(v -> {
            if (listener != null) listener.onMarcarCompleto(v, ex);
        });
    }

    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvRepeticoes, tvDescricao, btnCompleto;
        ImageView imgExercicio; // Campo de imagem

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_item_nome);
            tvRepeticoes = itemView.findViewById(R.id.tv_item_repeticoes);
            tvDescricao = itemView.findViewById(R.id.tv_item_descricao);
            btnCompleto = itemView.findViewById(R.id.btn_item_completo);

            // Componente ligado à tela
            imgExercicio = itemView.findViewById(R.id.img_item_exercicio);
        }
    }
}