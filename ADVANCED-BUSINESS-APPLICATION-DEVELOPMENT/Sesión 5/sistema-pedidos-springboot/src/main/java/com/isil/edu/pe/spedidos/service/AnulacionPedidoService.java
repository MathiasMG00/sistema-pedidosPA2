package com.isil.edu.pe.spedidos.service;

import java.util.List;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;

public interface AnulacionPedidoService {
    
    AnulacionPedido anularPedido(Long pedidoId, String motivo);
    List<AnulacionPedido> listarAnulaciones();
}
