package com.udemy.springbootreactor.app.models;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {
    private List<String> comentarios;

    public Comentarios() {
        comentarios = new ArrayList<>();
    }

    public void addComentario(String comentario) {
        comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "comentarios=" + comentarios;
    }
}
