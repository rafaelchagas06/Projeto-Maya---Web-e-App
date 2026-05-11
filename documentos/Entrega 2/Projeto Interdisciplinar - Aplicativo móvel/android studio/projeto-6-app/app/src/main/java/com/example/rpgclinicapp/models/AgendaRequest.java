package com.example.rpgclinicapp.models;

public class AgendaRequest {
    private int id_paciente;
    private String data;
    private String horario;

    public AgendaRequest(int id_paciente, String data, String horario) {
        this.id_paciente = id_paciente;
        this.data = data;
        this.horario = horario;
    }

    public int getId_paciente() {
        return id_paciente;
    }

    public void setId_paciente(int id_paciente) {
        this.id_paciente = id_paciente;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
