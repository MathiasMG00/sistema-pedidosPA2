package com.isil.edu.pe.spedidos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "anulacion_pedido")
public class AnulacionPedido {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column (name = "producto_codigo", nullable = false, length = 30)
    private String productoCodigo;

    @Column (name = "cantidad_devuelta", nullable = false)
    private int cantidadDevuelta;

    @Column (nullable = false)
    private LocalDateTime fecha;

    @Column (length = 200)
    private String motivo;

    public AnulacionPedido() {
    }

    public AnulacionPedido(Long pedidoId, String productoCodigo, int cantidadDevuelta,
            String motivo) {
        this.pedidoId = pedidoId;
        this.productoCodigo = productoCodigo;
        this.cantidadDevuelta = cantidadDevuelta;
        this.fecha = LocalDateTime.now();
        this.motivo = motivo;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getProductoCodigo() {
        return productoCodigo;
    }

    public int getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }
    
}
