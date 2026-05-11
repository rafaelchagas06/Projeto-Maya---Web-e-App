package com.example.rpgclinicapp.models;

public class AgendaResponse {
    private boolean sucesso;
    private String mensagem;
    private Detalhes detalhes;
    private String erro;

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Detalhes getDetalhes() {
        return detalhes;
    }

    public String getErro() {
        return erro;
    }

    public class Detalhes {
        private String data;
        private String horario;
        private int id_paciente;

        public String getData() {
            return data;
        }

        public String getHorario() {
            return horario;
        }

        public int getId_paciente() {
            return id_paciente;
        }
    }
}
