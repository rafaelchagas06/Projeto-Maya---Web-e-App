package com.example.rpgclinicapp.models;

public class Notificacao {
    private Long id;
    private Long paciente_id;
    private String titulo;
    private String mensagem;
    private Boolean lida;
    private String tipo;
    private String created_at;

    // Construtor vazio necessário para o Retrofit
    public Notificacao() {}

    public Long getId() { return id; }
    public Long getPaciente_id() { return paciente_id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public Boolean getLida() { return lida; }
    public String getTipo() { return tipo; }
    public String getCreated_at() { return created_at; }

    public void setLida(Boolean lida) { this.lida = lida; }
}