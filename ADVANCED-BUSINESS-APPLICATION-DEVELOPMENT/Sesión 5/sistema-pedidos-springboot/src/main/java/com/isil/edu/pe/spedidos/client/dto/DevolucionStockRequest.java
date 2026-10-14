package com.isil.edu.pe.spedidos.client.dto;

public class DevolucionStockRequest {
    private Integer cantidad;

    public DevolucionStockRequest() {
    }

    public DevolucionStockRequest(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

}
