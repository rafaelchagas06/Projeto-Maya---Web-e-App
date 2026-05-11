package com.example.rpgclinicapp.models;

public class CheckinRequest {
    private long paciente_id;
    private String paciente_nome;
    private String exercicio_nome;
    private int dor;
    private String comentario; // ADICIONADO

    public CheckinRequest(long paciente_id, String paciente_nome, String exercicio_nome, int dor, String comentario) {
        this.paciente_id = paciente_id;
        this.paciente_nome = paciente_nome;
        this.exercicio_nome = exercicio_nome;
        this.dor = dor;
        this.comentario = comentario; // ADICIONADO
    }

    // Getters e Setters (Importante para o Retrofit conseguir ler os dados)
    public long getPaciente_id() { return paciente_id; }
    public void setPaciente_id(long paciente_id) { this.paciente_id = paciente_id; }

    public String getPaciente_nome() { return paciente_nome; }
    public void setPaciente_nome(String paciente_nome) { this.paciente_nome = paciente_nome; }

    public String getExercicio_nome() { return exercicio_nome; }
    public void setExercicio_nome(String exercicio_nome) { this.exercicio_nome = exercicio_nome; }

    public int getDor() { return dor; }
    public void setDor(int dor) { this.dor = dor; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}