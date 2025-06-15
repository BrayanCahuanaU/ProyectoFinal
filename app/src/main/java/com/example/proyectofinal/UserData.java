package com.example.proyectofinal;

public class UserData {
    private String email;
    // Añade aquí más campos según necesites

    public UserData() {}  // Necesario para Firestore

    public UserData(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}