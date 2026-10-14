# Cambios realizados en *sistema-pedidos-springboot* 

## Requisitos
Agregar la anulacion de pedidos. Pedidos solicita a *inventario-service* la devolución de la cantidad reservada, conserva el pedido con estado anulado y registra la anulacion en H2.
La apliacion no accede directamente a la base de datos InventarioDB.

## Modificaciones

| Archivos | Modificación de las entidades |
|---|---|
| `entity/PedidoEstado.java` | Enum para los estados de REGISTRADO y ANULADO. |
| `entity/Pedido.java` | Se agregó el estado inicial como REGISTRADO y el metodo `anular()`. |
| `entity/AnulacionPedido.java` | Nueva entidad para los datos de la anulación. |

| Archivos | Modificación en los repositorios y servicios |
|---|---|
| `repository/AnulacionPedidoRepository.java` | Extiende JpaRepository e incorpora metodos que Spring Data implementa. |
| `service/AnulacionPedidoService.java` | Contrato para anular pedidos y listar anulaciones. |
| `service/impl/AnulacionPedidoServiceImpl.java` | Valida datos y estado, evita duplicados secuenciales, solicita devolución remota y guarda los cambios locales. |

| Archivos | Modificación en los client |
|---|---|
| `client/DevolucionStockClient.java` | Interfaz de integración con el método devolucionStock(codigo, cantidad). |
| `client/DevolucionStockRestClient.java` | Implementación HTTP administrada con @Component; reutiliza inventarioHttpClient y la URL configurable. |
| `client/dto/DevolucionStockRequest.java` | Contrato JSON de salida hacia Inventario: cantidad. |
| `client/dto/DevolucionStockResponse.java` | Contrato JSON recibido de Inventario con los datos de la devolución. |

| Archivos | Modificaciones en los controladores |
|---|---|
| `controller/dto/AnulacionPedidoRequest.java` | Solicitud del usuario con pedidoId y motivo. |
| `controller/dto/AnulacionPedidoResponse.java` | Respuesta REST de la anulación; convierte la entidad mediante from(). En la versión actual el campo JSON se llama cantidad. |
| `controller/AnulacionPedidoApiController.java` | Expone POST y GET /api/v1/anulaciones; inyecta el servicio por constructor. |

| Archivos | Modificación de las excepciones controladas |
|---|---|
| `controller/AnulacionPedidoExceptionHandler.java` | Advice limitado al Controller de anulaciones; devuelve errores mediante ProblemDetail. |
| `exception/PedidoException.java` | Incorpora TipoAnulacion: DATOS_INVALIDOS, PEDIDO_NO_EXISTE y PEDIDO_YA_ANULADO. |
| `client/ClientException.java` | Conserva el estado HTTP y mensaje del error remoto. |
| `client/InventarioNoDisponibleException.java` | Representa fallos técnicos, de conexión y timeout; se reutiliza para errores remotos 5xx traducidos por el servicio. |

## Flujo de anulacion

1. El Controlador recibe pedidoId y motivo, y lo delega al servicio.
2. `validarDatos()` comprueva el id positivo y la cantidad de caracteres del motivo (<200)
3. PedidoRepository, busca el pedido; si no existe rechaza la solicitud.
4. El servicio comprueva el estado actual del pedido no sea ANULADO.
5. DevolucionStockClient, envía el codigo y la cantidad del pedido al inventario.
6. Si se procesa correctamente, se crea AnulacionPedido y se cambia el estado actual a ANULADO.
7. Se guarda el pedido y se ejecuta saveAndFlush sobre la anulacion.
8. El controlador devuelve el DTO con HTTP 201.

## Enpoints y ejemplos 

### Anular un pedido

POST `http://localhost:8081/sistema-pedidos/api/v1/anulaciones`

Formato de solicitud:
```http
Content-Type: application/json
{
  "pedidoId": 1,
  "motivo": "Solicitud del cliente"
}
```

Ejemplo de resultado esperado:
```json
{
  "id": 1,
  "pedidoId": 1,
  "productoCodigo": "LAP-001",
  "cantidad": 2,
  "fecha": "2026-10-14T00:46:02.1549079",
  "motivo": "Solicitud del cliente"
}
```
Si el motivo es nulo o vacío, el servicio almacena "Anulación sin motivo".

### Listar anulaciones

GET `http://localhost:8081/sistema-pedidos/api/v1/anulaciones`

Responde HTTP 200 con una lista de AnulacionPedidoResponse; si no hay registros devuelve una lista vacía.

### Enpoint remoto utilizado

POST `http://localhost:8082/inventario-service/api/invetario/productos/codigo/{codigo}/devoluciones`

Formato de solicitud:
```http
Content-Type: application/json
{
  "cantidad": 2
}
```
El código viaja en la ruta. La respuesta incluye codigo, cantidadDevuelta y stockRestante. Cada solicitud válida incrementa stock.

## Tratamiento de errores

| Situación | HTTP del API de anulaciones |
|---|---:|
| ID inválido o motivo mayor a 200 caracteres | 400 |
| JSON inválido o cuerpo ausente | 400 |
| Pedido inexistente | 404 |
| Pedido ya anulado o registro previo de anulación | 409 |
| Inventario responde 4xx y se propaga ClientException | 502 |
| Inventario responde 5xx, traducido por el servicio | 503 |
| Conexión o timeout, representado por InventarioNoDisponibleException | 503 |

El cliente intenta leer ApiError para conservar el mensaje remoto; si no puede interpretarlo mantiene un mensaje genérico. El Handler utiliza ProblemDetail para los errores del API de Pedidos.

## Pruebas unitarias nuevas

En la clase `service/impl/AnulacionesPedidoServiceImplTest.java` dentro de test.

| Prueba | Verificación |
|---|---|
| debeAnularPedidoYDevolverStock | Verificar devolución remota, cambio de estado y registro de anulación. |
| debeRechazarPedidoYaAnuladoSinDevolverStock | Verificar rechazo sin otra devolución ni escrituras posteriores. |
| noDebeGuardarCambiosCuandoInventarioFalla | Verificar que el fallo remoto conserva el estado y evita el guardado local. |

Comando de ejecución desde la raíz del proyecto:

```powershell
mvn test
```