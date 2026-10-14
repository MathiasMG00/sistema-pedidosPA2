package com.isil.edu.pe.spedidos.controller.dto;

import java.time.LocalDateTime;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;

public record AnulacionPedidoResponse(
    Long id,
    Long pedidoId,
    String productoCodigo,
    int cantidad,
    LocalDateTime fecha,
    String motivo
) {
    public static AnulacionPedidoResponse from(AnulacionPedido anulacion){
        return new AnulacionPedidoResponse(
            anulacion.getId(), 
            anulacion.getPedidoId(), 
            anulacion.getProductoCodigo(), 
            anulacion.getCantidadDevuelta(), 
            anulacion.getFecha(), 
            anulacion.getMotivo()
        );
    }
}
