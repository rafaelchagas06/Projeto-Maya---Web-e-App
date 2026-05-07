package com.example.rpgclinicapp.models;

// IMPORTANTE: Essa linha avisa o Retrofit como ler os nomes das colunas do banco
import com.google.gson.annotations.SerializedName;

public class Exercicio {

    private int id;

    // O banco manda "titulo", o Java guarda em "nome"
    @SerializedName("titulo")
    private String nome;

    private String descricao;

    // Se você tiver uma coluna de repetições no banco, coloque o nome exato dela aqui.
    // Se não tiver, não tem problema, o Java vai deixar vazio.
    private String repeticoes;

    // --- PREPARANDO PARA A IMAGEM ---
    // Substitua "url_imagem" pelo nome EXATO da coluna que guarda o link da foto/vídeo no Supabase
    @SerializedName("url_imagem")
    private String urlImagem;


    // --- Getters (Para o Android conseguir ler as informações) ---
    public int getId() { return id; }

    public String getNome() { return nome; }

    public String getDescricao() { return descricao; }

    public String getRepeticoes() { return repeticoes; }

    public String getUrlImagem() { return urlImagem; }
}