package com.isil.edu.pe.spedidos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.client.ClientException;
import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;

@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

    private final PedidoRepository pedidoRepo;
    private final AnulacionPedidoRepository anulacionRepo;
    private final DevolucionStockClient devolucionClient;

    public AnulacionPedidoServiceImpl(PedidoRepository pedidoRepo, AnulacionPedidoRepository anulacionRepo,
            DevolucionStockClient devolucionClient) {
        this.pedidoRepo = pedidoRepo;
        this.anulacionRepo = anulacionRepo;
        this.devolucionClient = devolucionClient;
    }

    @Override
    @Transactional
    public AnulacionPedido anularPedido(Long pedidoId, String motivo) {
        validarDatos(pedidoId, motivo);

        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new PedidoException(
                        PedidoException.TipoAnulacion.PEDIDO_NO_EXISTE,
                        "No existe el pedido."));

        if (pedido.getEstado() != PedidoEstado.REGISTRADO
                || anulacionRepo.existsByPedidoId(pedidoId)) {
            throw new PedidoException(
                    PedidoException.TipoAnulacion.PEDIDO_YA_ANULADO,
                    "El pedido ya ha sido anulado.");
        }

        try {
            devolucionClient.devolucionStock(pedido.getProductoCodigo(), pedido.getCantidad());
        } catch (ClientException e) {
            if (e.getStatus() >= 500) {
                throw new InventarioNoDisponibleException(
                        "El servicio de devolución de stock presentó un error temporal (HTTP "
                                + e.getStatus()
                                + ").",
                        e);
            }
            throw e;
        }

        AnulacionPedido anulacion = new AnulacionPedido(
                pedido.getId(),
                pedido.getProductoCodigo(),
                pedido.getCantidad(),
                motivo == null || motivo.isBlank() ? "Anulación sin motivo" : motivo.trim());
        pedido.anular();
        pedidoRepo.save(pedido);

        return anulacionRepo.saveAndFlush(anulacion);
    }

    @Override
    public List<AnulacionPedido> listarAnulaciones() {
        return anulacionRepo.findAllByOrderByIdDesc();
    }

    private void validarDatos(Long pedidoId, String motivo) {
        if (pedidoId == null || pedidoId <= 0) {
            throw new PedidoException(
                    PedidoException.TipoAnulacion.DATOS_INVALIDOS,
                    "El identificador del pedido debe ser positivo.");
        }

        if (motivo != null && motivo.length() > 200) {
            throw new PedidoException(
                    PedidoException.TipoAnulacion.DATOS_INVALIDOS,
                    "El motivo no puede superar los 200 caracteres.");
        }
    }
}
