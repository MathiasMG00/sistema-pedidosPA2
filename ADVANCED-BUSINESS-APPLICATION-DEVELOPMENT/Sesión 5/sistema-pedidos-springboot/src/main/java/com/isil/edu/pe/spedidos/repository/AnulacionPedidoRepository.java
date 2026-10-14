package com.isil.edu.pe.spedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;

public interface AnulacionPedidoRepository extends JpaRepository<AnulacionPedido, Long>{

    Optional<AnulacionPedido> findByPedidoId(Long pedidoId);
    List<AnulacionPedido> findAllByOrderByIdDesc();
    Boolean existsByPedidoId(Long pedidoId);
}
