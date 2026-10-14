package com.isil.edu.pe.spedidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;

@RestController 
@RequestMapping ("/api/v1/anulaciones")
public class AnulacionPedidoApiController {

    private final AnulacionPedidoService anulacionService;

    public AnulacionPedidoApiController(AnulacionPedidoService anulacionService){
        this.anulacionService = anulacionService;
    }

    @PostMapping 
    public ResponseEntity<AnulacionPedidoResponse> anular(
        @RequestBody AnulacionPedidoRequest request
    ){
        AnulacionPedido anulacion = anulacionService.anularPedido(
            request.pedidoId(), request.motivo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AnulacionPedidoResponse.from(anulacion));    
    }

    @GetMapping 
    public List<AnulacionPedidoResponse> listar(){
        return anulacionService.listarAnulaciones()
            .stream()
            .map(AnulacionPedidoResponse::from)
            .toList();
    }
}
