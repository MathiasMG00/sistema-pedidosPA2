package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.isil.edu.pe.spedidos.client.ClientException;
import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;

public class AnulacionPedidoServiceImplTest {

    private PedidoRepository pedidoRepo;
    private AnulacionPedidoRepository anulacionRepo;
    private DevolucionStockClient devolucionClient;
    private AnulacionPedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        pedidoRepo = mock(PedidoRepository.class);
        anulacionRepo = mock(AnulacionPedidoRepository.class);
        devolucionClient = mock(DevolucionStockClient.class);

        service = new AnulacionPedidoServiceImpl(pedidoRepo, anulacionRepo, devolucionClient);
    }

    @Test
    void debeAnularPedidoYDevolverStock() {
        Pedido pedido = nuevoPedido();

        DevolucionStockResponse dev = new DevolucionStockResponse();

        dev.setCodigo("LAP-001");
        dev.setCantidadDevuelta(2);
        dev.setStockRestante(10);

        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        when(anulacionRepo.existsByPedidoId(1L)).thenReturn(false);
        when(devolucionClient.devolucionStock("LAP-001", 2)).thenReturn(dev);
        when(anulacionRepo.saveAndFlush(any(AnulacionPedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnulacionPedido result = service.anularPedido(1L, "Solicitud del cliente");

        assertEquals(PedidoEstado.ANULADO, pedido.getEstado());
        assertEquals(1L, result.getPedidoId());
        assertEquals("LAP-001", result.getProductoCodigo());
        assertEquals(2, result.getCantidadDevuelta());
        assertEquals("Solicitud del cliente", result.getMotivo());
        assertNotNull(result.getFecha());

        verify(devolucionClient, times(1)).devolucionStock("LAP-001", 2);
        verify(pedidoRepo, times(1)).save(pedido);
        verify(anulacionRepo, times(1)).saveAndFlush(result);
    }

    @Test
    void debeRechazarPedidoYaAnuladoSinDevolverStock() {
        
        Pedido pedido = nuevoPedido();
        pedido.anular();

        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoException error = assertThrows(
                PedidoException.class,
                () -> service.anularPedido(1L, "Segundo intento")
            );

        assertEquals(
                PedidoException.TipoAnulacion.PEDIDO_YA_ANULADO,
                error.getTipo());
        assertEquals(PedidoEstado.ANULADO, pedido.getEstado());

        verifyNoInteractions(devolucionClient);
        verify(pedidoRepo, never()).save(any(Pedido.class));
        verify(anulacionRepo, never()).saveAndFlush(any(AnulacionPedido.class));
        verify(anulacionRepo, never()).save(any(AnulacionPedido.class));

    }

    @Test
    void noDebeGuardarCambiosCuandoInventarioFalla() {

        Pedido pedido = nuevoPedido();
        ClientException falloRemoto = new ClientException(
                500,
                "Error interno de Inventario.");

        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        when(anulacionRepo.existsByPedidoId(1L)).thenReturn(false);
        when(devolucionClient.devolucionStock("LAP-001", 2)).thenThrow(falloRemoto);

        InventarioNoDisponibleException error = assertThrows(
                InventarioNoDisponibleException.class,
                () -> service.anularPedido(1L, "Solicitud del cliente")
            );

        assertSame(falloRemoto, error.getCause());
        assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado());

        verify(devolucionClient, times(1)).devolucionStock("LAP-001", 2);
        verify(pedidoRepo, never()).save(any(Pedido.class));
        verify(anulacionRepo, never()).saveAndFlush(any(AnulacionPedido.class));
        verify(anulacionRepo, never()).save(any(AnulacionPedido.class));
        
    }

    private Pedido nuevoPedido() {
        Pedido pedido = new Pedido(
                "Ana Torres",
                "LAP-001",
                "Laptop",
                new BigDecimal("2500.00"),
                2,
                new BigDecimal("5000.00"));
        pedido.setId(1L);
        return pedido;
    }
}
