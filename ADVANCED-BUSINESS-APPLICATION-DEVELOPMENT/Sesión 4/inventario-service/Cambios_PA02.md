# Cambios realizados en *inventario-service* 

## Requisitos

Implementar el endpoint REST de devolución de stock de un producto.

## Modificaciones

| Archivos | Modificación |
|---|---|
| `DevolucionStockRequest.java` y `DevolucionStockResponse.java` | DTO para la solicitud y respuesta de devolución. |
| `ProductoInventario.java` | Se agregó `devolverStock()` con su validación. |
| `InventarioService.java` | Se agregó la devolución por código. |
| `ProductoResource.java` | Se agregó el endpoint POST de devolución de stock. |
| `ProductoInventarioTest.java` | Se agregaron pruebas de devolución válida e inválida. |
| `pom.xml` | Se agregó la dependencia del controlador JDBC de SQL Server. |

## Endpoint de devolución

POST `/inventario-service/api/inventario/productos/codigo/{codigo}/devoluciones`

Formato de solicitud:
```http
Content-Type: application/json
{
  "cantidad": 2
}
```

Ejemplo: 
```
Teniendo los siguientes datos para devolución.
codigo = 'LAP-001' 
cantidad = 2
```

Resultado esperado:
```json
{
    "cantidadDevuelta": 2,
    "codigo": "LAP-001",
    "nombre": "Laptop",
    "productoId": 1,
    "stockRestante": 8,
    "version": 3
}
```
## Pruebas unitarias nuevas

En la clase `ProductoInventarioTest.java`.

| Prueba | Verificación |
|---|---|
| `debeDevolverStock` | Una devolución de 3 unidades incrementa el stock de 6 a 9. |
| `noDebePermitirDevolverStock` | Cero y negativos generan una excepción y mantienen el stock en 6. |

Comando de ejecución desde la raíz del proyecto:

```powershell
mvn test
```