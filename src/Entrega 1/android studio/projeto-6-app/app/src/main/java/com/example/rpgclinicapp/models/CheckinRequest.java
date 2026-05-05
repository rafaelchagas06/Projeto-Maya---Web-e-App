package com.example.rpgclinicapp.models;

public class CheckinRequest {
    private long paciente_id;
    private String paciente_nome;
    private String exercicio_nome;
    private int dor;

    public CheckinRequest(long paciente_id, String paciente_nome, String exercicio_nome, int dor) {
        this.paciente_id = paciente_id;
        this.paciente_nome = paciente_nome;
        this.exercicio_nome = exercicio_nome;
        this.dor = dor;
    }
}