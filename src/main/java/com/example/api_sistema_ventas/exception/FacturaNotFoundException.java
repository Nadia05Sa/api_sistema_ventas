package com.example.api_sistema_ventas.exception;

public class FacturaNotFoundException extends RuntimeException {
    public FacturaNotFoundException(String message) {
        super(message);
    }
}
