package com.isil.edu.pe.spedidos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.isil.edu.pe.spedidos.client.ClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.exception.PedidoException;

@RestControllerAdvice(assignableTypes = AnulacionPedidoApiController.class)
public class AnulacionPedidoExceptionHandler {

    @ExceptionHandler(PedidoException.class)
    public ResponseEntity<ProblemDetail> manejarPedido(PedidoException e) {
        
        HttpStatus status;

        if (e.getTipo() == null) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = switch (e.getTipo()) {
                case DATOS_INVALIDOS -> HttpStatus.BAD_REQUEST;
                case PEDIDO_NO_EXISTE -> HttpStatus.NOT_FOUND;
                case PEDIDO_YA_ANULADO -> HttpStatus.CONFLICT;
            };
        }

        return crearRespuesta(status, e.getMessage());
    }

    @ExceptionHandler(InventarioNoDisponibleException.class)
    public ResponseEntity<ProblemDetail> manejarIndisponibilidad(
            InventarioNoDisponibleException e) {
        return crearRespuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                e.getMessage());
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ProblemDetail> manejarCliente(
            ClientException e) {
        // Un error del contrato remoto no significa que el pedido no exista.
        return crearRespuesta(
                HttpStatus.BAD_GATEWAY,
                "Inventario rechazó la devolución: " + e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> manejarJson(
            HttpMessageNotReadableException e) {
        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "El cuerpo debe ser un JSON válido con pedidoId y motivo opcional.");
    }

    private ResponseEntity<ProblemDetail> crearRespuesta(
            HttpStatus status,
            String mensaje) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(
                status,
                mensaje);
        error.setTitle(status.getReasonPhrase());

        return ResponseEntity.status(status).body(error);
    }
}