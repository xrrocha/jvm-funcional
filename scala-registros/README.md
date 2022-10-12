### `SRegistros`: Ejemplo de DSL Funcional en Scala

![tl-dr;](docs/img/tl-dr.png)
Este repositorio ilustra el diseño, implementación y uso de un lenguaje de dominio específico (DSL) en Scala 3
para copiar datos desde/hacia bases de datos, archivos planos, servidores y otros formatos.
Para ello se emplean patrones funcionales simples alrededor de una simplificación del modelo _map/reduce_.

### _Map/Reduce_

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
  generar =
    compras.iterator,
  transformar = 
    compra => compra.cantidad * compra.precioUnitario,
  reducir = 
    _.sum
)

// Imprime: "El total de compras es 9.6"
println(s"El total de compras es $totalCompras")
```

Nuestro DSL de copia de registros está construido alrededor de esta simple, pero potente, forma de _map/reduce_.

El punto más importante a tener en cuenta para la discusión de nuestra herramienta es que 
**enfatizamos el uso de _funciones_ por encima del uso de clases e interfaces**.

Es decir: sacamos partido las capacidades _funcionales_ de Scala apelando a sus capacidades orientadas a objetos 
solo donde estas son apropiadas. 

### Copia de Registros

Nuestro propósito es definir un vocabulario que permita al desarrollador expresar _declarativamente_ operaciones de 
extracción y diseminación de datos en una variedad de formatos.

Ejemplos:

- Extraer datos de una base de datos relacional y colocar los resultados CSV en un servidor SFTP remoto
- Leer datos a partir de un archivo de longitud fija y almacenarlos en una base de datos relacional
- Leer datos de un archivo delimitado por _tabs_ y generar un libro Excel en otro archivo local

Requerimos leer datos de una variedad de fuentes y formatos a la vez que también requerimos escribir datos en la misma 
variedad de destinos y formatos. 

Esto hace necesario definir un _formato intermedio_ uniformemente generado por los lectores y consumido por los 
escritores. 

> 👉 De esta forma, dados `M` formatos de entrada y `N` formatos de salida necesitaremos solo `M+N` combinaciones 
> en vez de `M*N`!

Llamaremos a este formato intermedio _registro_ y lo representaremos como un mapa de nombres de campo a valores de 
campo. Simbólicamente:

```scala
type Registro = Map[String, _]
```

La existencia de este formato intermedio nos lleva a refinar nuestra versión original de _map/reduce_ para:

- Renombrar la operación de `mapReduce` a `copiar`
- Renombrar también las funciones pasadas como parámetros a fin de enfatizar su uso en operación de copia
- Añadir un argumento de extracción que convierte del tipo de datos de entrada `E` al registro `Map[String,_]`

Así, nuestro _framework_ de copia se reformula como:

```scala
def copiar[E, S](leer: => Iterator[E],
                 extraer: E => Map[String, _],
                 transformar: Map[String, _] => Map[String, _],
                 recolectar: Iterator[Map[String, _]] => S): S =
  recolectar(leer.map(extraer.andThen(transformar)))
```

Para ilustrar el tipo de DSL declarativo que queremos formular consideremos el requerimiento de convertir el 
siguiente archivo delimitado `personas.csv`:

```
janet,doe,1000.25
john,doe,750.5
```

en el siguiente archivo de longitud fija `personas.dat`:

```
janet   doe     100025
john    doe     075050
```

Esta transformación se formularía como:

```scala
copiar(
      leyendoLineas(File("personas.csv")),
  
      extrayendoCon(
        delimitadorEntrada(","),
        campoEntradaDelimitado("nombre", posicion = 0),
        campoEntradaDelimitado("apellido", 1),
        campoEntradaDelimitado("saldo", 2, extraer = _.toDouble),
      ),
  
      recolectandoCon(
        registroFijo(longitud = 24),
        recolectorFijo(File("personas.dat")),
        campoSalidaFijo("nombre", posicion = 0, longitud = 8),
        campoSalidaFijo("apellido", 8, 8),
        campoSalidaFijo("saldo", 16, 6, colocar = formatoNumerico("000000", 100))
      )
)
```


### Funciones de Dominio Específico

En el ejemplo anterior, los argumentos pasados como gerundios (`leyendLineas`, `extrayendoCon`, `recolectandoCon`) 
invocan _funciones de orden superior_ que construyen y retornan otras funciones (aquellas que la función `copiar`
espera como argumentos).

> 👉 El patrón de escribir funciones que retornan otras funciones es muy común en programación funcional 
>    pero no lo es (todavía) en la programación orientada a objetos, si bien es el del todo posible.

Estas funciones de orden superior son las que definen el lenguaje de dominio específico como tal. 

La responsabilidad primaria de estas funciones es aquella de traducir el _qué_ al _cómo_. Esto le permite al
desarrollador _declarar_ qué quiere lograr en vez de _deletrear_ cómo debe hacerse.

Consideremos la función `leyendoLineas` utilizada en el ejemplo anterior:

```scala
copiar(
    leer = leyendoLineas(java.io.File("personas.csv")),
    . . .
)
```

Recordemos que el argumento `leer` de la función `copiar` es una función que retorna un iterador de ítems de entrada: 
`=> Iterator[E]`

Así, pues, la función `leyendoLineas` retorna otra función que, al ser finalmente invocada, retorna un iterador de 
ítems de entrada. 

Cuántos niveles de indirección! 😉

Pero es en esta indirección donde reside el poder de la composición funcional: posiblita manipular las funciones como 
datos y combinarlas selectivamente para lograr efectos que, implementados de forma imperativa, requerirían 
repetición mecánica de código y "juiciosa aplicación de patrones de diseño".

Afortunadamente, la sintaxis _call by name_ de Scala permite definir estas funciones de manera que retornen 
directamente los valores esperados.  

Veamos:

```scala
def leyendoLineas(archivo: File): Iterator[String] =
  leyendoLineas(FileReader(archivo))

def leyendoLineas(lector: Reader): Iterator[String] = new Iterator[String] :
    private val lectorLineas = BufferedReader(lector)
    private var linea = lectorLineas.readLine()
    
    override def hasNext: Boolean = linea != null
    
    override def next(): String =
        val lineaAnterior = linea
        linea = lectorLineas.readLine()
        lineaAnterior
```

Como se aprecia, la función `leyendoLineas` es un adaptador que _transforma_ un archivo en un iterador de las líneas 
contenidas en ese archivo.

En este mismo espíritu, la función `extrayendoCon` sintetiza una función que transforma las líneas retornadas por 
`leyendoLineas` en registros de tipo `Map[String, _]`:

```scala
extrayendoCon(
    delimitadorEntrada(","),
    campoEntradaDelimitado("nombre", posicion = 0),
    campoEntradaDelimitado("apellido", 1),
    campoEntradaDelimitado("saldo", 2, extraer = _.toDouble),
)
```

> 👉 Esta es una de las más importantes diferencias entre los estilos imperativo y funcional: 
> donde el programador imperativo _instruye_ al computador, en tiempo de compilación, para que este ejecute una serie 
> estática de pasos,
> el programador funcional usa una formulación declarativa para _derivar_, en tiempo de ejecución, los pasos que el 
> computador finalmente debe ejecutar.




