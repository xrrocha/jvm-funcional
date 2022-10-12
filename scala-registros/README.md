### `SRegistros`: Ejemplo de DSL Funcional en Scala

![tl-dr;](docs/img/tl-dr.png)
Este repositorio ilustra el diseño, implementación y uso de un lenguaje de dominio específico (DSL) en Scala 3
para copiar datos desde/hacia bases de datos, archivos planos, servidores y otros formatos.
Para ello se emplean patrones funcionales simples alrededor de una simplificación del modelo _map/reduce_.

La idea esencial de la operación _map/reduce_ es muy simple: un generador produce una secuencia de ítems de datos que 
son subsiguientemente transformados (_map_) y luego agregados (_reduce_) en un resultado final. Una formulación 
simplificada de este modelo sería:

```scala
/**
 * Implementación minimalista de `mapReduce`.
 * @param generar Función sin argumentos que retorna un iterador del tipo de entrada `E`
 * @param transformar Función que transforma un ítem de entrada `E` en tipo intermedio `T`
 * @param reducir Función que agrega ítems de tipo intermedio `T` en un único ítem de salida `S`
 * @tparam E Tipo de datos de entrada
 * @tparam T Tipo de datos intermedio
 * @tparam S Tipo de datos de salida
 * @return Instancia de tipo de datos de salida `S` resultante del proceso
 */
def mapReduce[E, T, S](generar: => Iterator[E], 
                       transformar: E => T, 
                       reducir: Iterator[T] => S) = reducir(generar.map(transformar))
```

> 👉 Esta simplificación se aparta de la idea "clásica" de _map/reduce_ 
> (como aquella [popularizada por Google](https://es.wikipedia.org/wiki/MapReduce))
> en que omitimos su aspecto de paralelización.
> Esto es apropiada para nuestro más mundano propósito de, simplemente, "copiar registros".

Un ejemplo simple de uso de esta función de _map/reduce_ sería: 

```scala
case class Compra(item: String, cantidad: Int, precioUnitario: Double)

val compras = List(
  Compra("martillo", 1, 2.5),
  Compra("clavo", 12, 0.3),
  Compra("tornillo", 10, 0.35)
)

val totalCompras = mapReduce(
  generar = compras.iterator,
  transformar = compra => compra.cantidad * compra.precioUnitario,
  reducir = _.sum
)

// Imprime: "El total de compras es 9.6"
println(s"El total de compras es $totalCompras")
```

Nuestro DSL de copia de registros está construido alrededor de esta simple, pero potente, forma de _map/reduce_.

El punto más importante a tener en cuenta para la discusión de nuestra herramienta es que 
**enfatizamos el uso de _funciones_ por encima del uso de clases e interfaces**.

Es decir: sacamos partido las capacidades _funcionales_ de Scala apelando a sus capacidades orientadas a objetos 
solo cuando estas resultan ser las más apropiadas. 

### Copia de Registros

Nuestro propósito es definir un vocabulario que permita al desarrollador expresar _declarativamente_ operaciones de 
extracción y diseminación de datos en una variedad de formatos.

Ejemplos:

- Extraer datos de una base de datos relacional y colocar los resultados CSV en un servidor SFTP remoto
- Leer datos a partir de un archivo de longitud fija y almacenarlos en una base de datos relacional
- Leer datos de un archivo delimitado por _tabs_ y generar un libro Excel en otro archivo local

