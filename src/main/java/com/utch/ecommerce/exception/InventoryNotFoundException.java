package com.utch.ecommerce.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String productId) {
        super("Inventario no encontrado: " + productId);
    }
}
