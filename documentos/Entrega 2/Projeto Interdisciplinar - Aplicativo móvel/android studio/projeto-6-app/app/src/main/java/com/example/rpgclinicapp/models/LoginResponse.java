package com.example.rpgclinicapp.models;

public class LoginResponse {
    private boolean sucesso;
    private String mensagem;
    private User usuario;
    private String token;
    private String erro;

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public User getUsuario() {
        return usuario;
    }

    public String getToken() {
        return token;
    }

    public String getErro() {
        return erro;
    }
}
