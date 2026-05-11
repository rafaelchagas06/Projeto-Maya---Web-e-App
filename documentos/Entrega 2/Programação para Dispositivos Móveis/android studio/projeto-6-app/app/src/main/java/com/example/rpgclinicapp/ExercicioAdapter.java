package com.example.rpgclinicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController; // <-- Importação do controlador de vídeo
import android.widget.TextView;
import android.widget.VideoView;
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

        if (ex.getNome() != null) holder.tvNome.setText(ex.getNome());
        if (ex.getRepeticoes() != null) holder.tvRepeticoes.setText(ex.getRepeticoes());
        if (ex.getDescricao() != null) holder.tvDescricao.setText(ex.getDescricao());

        // RESET DE SEGURANÇA: Garante que o vídeo pare e a foto volte se o usuário rolar a tela
        holder.videoExercicio.setVisibility(View.GONE);
        holder.videoExercicio.stopPlayback();

        // --- INÍCIO DA LÓGICA DA IMAGEM/VÍDEO ---
        String urlDaMidia = ex.getUrlImagem();

        if (urlDaMidia != null && !urlDaMidia.isEmpty()) {
            holder.imgExercicio.setVisibility(View.VISIBLE);

            // O Glide baixa a miniatura e salva no cache
            Glide.with(holder.itemView.getContext())
                    .load(urlDaMidia)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.holo_red_light)
                    .centerCrop()
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .into(holder.imgExercicio);

            // --- LÓGICA DE RODAR NO QUADRADINHO ---
            holder.imgExercicio.setOnClickListener(v -> {
                // Se for um vídeo (termina com .mp4), roda no lugar da foto
                if (urlDaMidia.toLowerCase().contains(".mp4")) {
                    holder.imgExercicio.setVisibility(View.GONE); // Esconde a foto
                    holder.videoExercicio.setVisibility(View.VISIBLE); // Mostra o vídeo

                    holder.videoExercicio.setVideoURI(android.net.Uri.parse(urlDaMidia));

                    // --- NOVO: Adiciona a barra de controles (Play/Pause) ---
                    MediaController mediaController = new MediaController(holder.itemView.getContext());
                    mediaController.setAnchorView(holder.videoExercicio);
                    holder.videoExercicio.setMediaController(mediaController);
                    // --------------------------------------------------------

                    // Faz o vídeo repetir automaticamente (loop)
                    holder.videoExercicio.setOnPreparedListener(mp -> mp.setLooping(true));

                    holder.videoExercicio.start(); // Dá o play
                }
                // Se for imagem, não fazemos nada no clique, pois ela já está sendo mostrada!
            });

        } else {
            // Se não tem nada, esconde os dois
            holder.imgExercicio.setVisibility(View.GONE);
            holder.videoExercicio.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext()).clear(holder.imgExercicio);
            holder.imgExercicio.setImageDrawable(null);
            holder.imgExercicio.setOnClickListener(null);
        }
        // --- FIM DA LÓGICA ---

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
        ImageView imgExercicio;
        VideoView videoExercicio; // Tocador de vídeo

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_item_nome);
            tvRepeticoes = itemView.findViewById(R.id.tv_item_repeticoes);
            tvDescricao = itemView.findViewById(R.id.tv_item_descricao);
            btnCompleto = itemView.findViewById(R.id.btn_item_completo);
            imgExercicio = itemView.findViewById(R.id.img_item_exercicio);

            // Ligando com o ID do item_exercicio.xml
            videoExercicio = itemView.findViewById(R.id.video_item_exercicio);
        }
    }
}