# Lambda Quito: Programación Funcional en la JVM

Este repositorio contiene el código y las notas de la presentación
[Programación Funcional en la Máquina Virtual de Java](https://github.com/xrrocha/jvm-funcional)
presentada para
[Quito Lambda](https://www.meetup.com/quito-lambda-meetup/)

## Plataforma Java: Lenguaje, Máquina Virtual y Ecosistema

El término _Java_ tiene acepciones diferentes (aunque relacionadas):

- El _lenguaje de programación_ Java
- La _máquina virtual_ Java (JVM)
- Las _especificaciones_ Java (JSR)
- El _ecosistema_ de software Java

En cuanto lenguaje de programación, Java es uno de los más exitosos e influyentes de la historia exhibiendo por igual 
características potentes y falencias pronunciadas. Java el lenguaje es un actor central en el desarrollo de software 
de nuestros tiempos: a la vez amado y vilipendiado pero nunca ignorado.

En lo tocante a la máquina virtual Java aun los más apasionados detractores del lenguaje conceden sin tardanza que 
la JVM es un triunfo de ingeniería en términos de portabilidad, eficiencia y robustez. Esto se debe, entre otros, a 
la riqueza y portabilidad de su [_bytecode_](https://en.wikipedia.org/wiki/Bytecode) orientado a objetos, la 
eficiencia de la conversión de este a código nativo 
([_just-in-time compilation_](https://en.wikipedia.org/wiki/Just-in-time_compilation), JIT) y la variedad y 
rendimiento de sus recolectores de basura 
([_garbage collection_](https://en.wikipedia.org/wiki/Garbage_collection_(computer_science))). 

Es importante notar que existen _múltiples_ implementaciones de la máquina virtual de Java: 
[Oracle](https://dev.java), 
[OpenJDK](https://jdk.java.net), 
[Android/Dalvik](https://source.android.com/docs/core/dalvik), 
[GraalVM](https://www.graalvm.org) entre otras. 
La gran mayoría de ellas corren indistintamente sobre multiples arquitecturas de procesador así como sobre múltiples 
sistemas operativos.

Una peculiaridad del entorno de desarrollo Java es la existencia de _especificaciones_ que se limitan a enunciar API's 
para múltiples dominios, pero que no proveen una implementación para ellas! Estas especificaciones (definidas 
mediante el 
[proceso comunitario de Java](https://jcp.org/en/home/index)) 
abarcan propósitos tan disímiles como el acceso uniforme a múltiples bases 
de datos relacionales
([JDBC](https://docs.oracle.com/javase/tutorial/jdbc/overview/)), 
el mapeo de objetos a diferentes tipos de bases de datos 
([JPA](https://www.oracle.com/technical-resources/articles/java/jpa.html), 
[JDO](https://www.oracle.com/java/technologies/javase/jdo.html)), la ejecución de 
transacciones distribuidas 
([JTA](https://docs.oracle.com/cd/B14099_19/web.1012/b14012/jta.htm)), 
la implementación de servicios HTTP 
([Java Servlets](https://www.oracle.com/java/technologies/servlet-technology.html) 
o el uso de cachés 
([Java Caching](https://docs.oracle.com/en/cloud/paas/app-container-cloud/cache/use-java-api-caching.html)).

Cuando se habla de Java en cuanto _plataforma_ se hace referencia a la combinación del lenguaje, la máquina virtual 
y las especificaciones de software. El uso de estas especificaciones permite a los usuarios programar contra API's 
abstractas y así poder cambiar de proveedores de software al no depender de sus implementaciones concretas.

A estos tres componentes se debe sumar otro más no por informal menos importante: el enorme _ecosistema de software_ 
desarrollado alrededor de Java por comunidades de todo tipo alrededor del mundo y, especialmente, en forma de código 
abierto y gratuito. Este variado ecosistema abarca prácticamente todo el espectro de la ingeniería de software: 
algoritmos, librerías, servidores, utilitarios, protocolos, bases de datos y (especialmente relevante para los 
propósitos de esta presentación) _lenguajes de programación_.

### La JVM como Plataforma de Lenguajes de Programación

Si bien Java es el lenguaje más difundido y empleado la JVM no es, ciertamente, el único. De hecho, (y esto puede 
sorprender incluso a los entusiastas de Java) puede afirmarse que no es el lenguaje más sofisticado de 
los disponibles sobre la JVM.

Muchos de los lenguajes que corren sobre la JVM son 
[reimplementaciones de lenguajes establecidos](https://en.wikipedia.org/wiki/List_of_JVM_languages) 
fuera de ella tales como Javascript, Python, Ruby, R, Go, Haskell o PHP. No obstante, ninguno de estos lenguajes 
"externos" compite con Java sobre la JVM. Con excepción de su uso para _scripting_, estos lenguajes no son de uso 
común en producción incluso si, en algunos casos, pueden correr tanto o más eficientemente sobre la JVM que sobre sus 
implementaciones originales!

De mayor relevancia para esta presentación son los lenguajes que fueron expresamente concebidos para correr sobre la 
JVM. Estos incluyen 
[Scala](https://www.scala-lang.org), 
[Kotlin](https://kotlinlang.org), 
[Clojure](https://clojure.org), 
[Groovy](https://groovy-lang.org), 
[Ceylon](https://www.ceylon-lang.org) y 
[Xtend](https://www.eclipse.org/xtend/) 
entre otros. La gran mayoría de estos lenguajes 
"nativos" exhiben capacidades funcionales.

Una evolución reciente es [GraalVM](https://www.graalvm.org) (de Oracle), una implementación de la JVM 
explícitamente concebida para ser "políglota" tanto a nivel de la máquina virtual como tal así como mediante el 
_framework_ [Truffle]() que simplifica la implementación de lenguajes para que generen _bytecode_ Java. Es de 
interés que GraalVM añade a la tradicional compilación JIT 
([_just-in-time_](https://en.wikipedia.org/wiki/Just-in-time_compilation)) 
la compilación anticipada 
([_ahead-of-time compilation_](https://en.wikipedia.org/wiki/Ahead-of-time_compilation), AOT) 
empleando [LLVM](https://llvm.org).

### La JVM como Plataforma de Lenguajes de Programación _Funcional_

Algunos de los lenguajes ya establecidos y que han sido portados a la JVM son lenguajes propiamente _funcionales_: 
[Haskell](https://www.haskell.org), 
[Scheme](http://www.scheme-reports.org) o 
[Standard ML](https://en.wikipedia.org/wiki/ML_(programming_language)). 
Estas reimplementaciones han tenido escaso éxito tanto en términos de su adopción como en la completitud de su 
implementación.

Los tres lenguajes _nativos_ de la JVM que sí disfrutan de gran difusión son, justamente, lenguajes completamente 
funcionales: Scala, Kotlin y Clojure. 

Estos tres lenguajes funcionales han trascendido la JVM y hoy compilan también a ejecutables nativos así como a 
Javascript. Esto último es relevante para el desarrollo de aplicaciones para el navegador web tanto empleando 
frameworks Javascript estándar (React, Angular, etc.) así como mediante (múltiples) frameworks desarrolladas en los 
lenguajes JVM mismos.

Una plataforma de gran importancia en relación con la JVM es Android, para el cual Google desarrolló su propia JVM:
[Dalvik](https://source.android.com/docs/core/dalvik). Luego de su litigio con Oracle, Google declaró a Kotlin como 
lenguaje "preferido" para construir aplicaciones Android. Esto catapultó el interés en Kotlin en el cliente y 
también ha estimulado grandemente su adopción en el así llamado _backend_.

### Programación Funcional JVM "Nativa"

> 👉 Por limitaciones de experiencia con Clojure por parte del autor esta presentación se centra en los lenguajes Scala, 
Kotlin y Java.

Tanto Scala como Kotlin implementan un estilo de programación que combina las bondades de la programación funcional 
con la orientación a objetos soportada por la JVM. 

Aunque con completo acceso a la librería estándar de Java, el lenguaje Clojure como tal no es orientado a objetos, 
siendo un lenguaje de tipo Lisp.

La JVM soporta, a nivel de _bytecode_, operaciones orientadas a objetos: despacho dinámico (o estático) de métodos, 
acceso a miembros de clase, etc. Por esta razón, un lenguaje funcional nativo de la JVM será preferencialmente un 
lenguaje "híbrido" que combine los paradigmas funcionales y de objetos que, por lo demás, se complementan de manera 
armónica y potente.

Por esta razón, fuera de la JVM, Kotlin y Scala solo serían comparables con otros lenguajes híbridos tales como 
[F#](https://fsharp.org) 
u 
[OCaml](https://ocaml.org). 
Estos lenguajes funcionales poseen también capacidades de objetos bien porque los "heredan" de su entorno 
(.NET en el caso de F#) o porque el mismo lenguaje las implementa directamente (OCaml). Otros lenguajes funcionales 
establecidos, como Haskell,
[Elixir](https://elixir-lang.org) o [Erlang](https://www.erlang.org),
serían menos directamente comparables por esta razón.

Dicho esto, Haskell fue una de las más fuertes influencias en el diseño de Scala. Scala, a su vez, ejerció gran 
influencia en el diseño de Kotlin que también recibió, a su vez, gran influencia de F#.

## Capacidades Funcionales Intrínsecas de la JVM

Java, por su parte, ha evolucionado para convertirse en un lenguaje apropiado para la programación funcional, aunque 
dentro de ciertas limitaciones que el programador funcional escrupuloso probablemente querría enfatizar en 
comparación con lenguajes funcionales "puros" como aquellos de la familia Haskell 
([Purescript](https://www.purescript.org), 
[Elm](https://elm-lang.org) o 
[Idris](https://www.idris-lang.org)).

El Java "moderno" ha adquirido (y continúa adquiriendo) capacidades funcionales. **Dado que toda característica de 
Java el lenguaje está soportada nativamente por la JVM estas nuevas capacidades son también "gratuitamente" 
accesibles a lenguajes funcionales como Kotlin, Scala y Clojure.**

Tales capacidades podrían proveer, también, una base para una reimplementación apropiada de otros lenguajes 
funcionales como, por ejemplo, variantes estrictas de Haskell.

## Lenguajes Funcionales JVM Exitosos: Java

La versión Java 1.5 de Java añadió tipos de datos genéricos. Si bien estos tipos genéricos son limitados al 
compararse con aquellos provistos por, por ejemplo, Haskell, sí tuvieron un enorme impacto en la evolución del 
lenguaje y vinieron acompañados de colecciones genéricas que pavimentaron el camino para la aparición de las 
capacidades funcionales de la versión 1.8.

La versión 1.8 fue la primera en introducir las capacidades normalmente asociadas con la programación funcional: 
_lambdas_, tipos de datos que modelan funciones invocables y operadores monádicos sobre colecciones (`filter`, 
`flatMap` o `reduce`).

Luego de la adquisición de Sun Microsystems (creador original de Java) por parte de Oracle este último impuso un 
ritmo mucho más rápido de evolución. La versión 1.8 se renombró como 8 y se fijó el lanzamiento de una nueva versión 
cada 6 meses.

Hoy Java se acerca a la versión 19 y tras años de acelerada evolución del lenguaje y de la JVM, exhibe 
características comunes en lenguajes funcionales como:

- Lambdas
- Inferencia de tipos (_var_)
- Tipos de datos inmutables (_records_)
- Coincidencia de patrones por tipo de datos (_pattern matching_ en vez de _instanceof_)
- Tipos de datos "sellados" (_sealed classes_)
- Coincidencia de patrones sobre `switch` (_destructuring_)
- Continuaciones y fibras ([Project Loom](https://cr.openjdk.java.net/~rpressler/loom/Loom-Proposal.html), Java 19)
- Optimización de llamadas recursivas  (_tail calls_, Project Loom, Java 19)

Dicho esto, la librería estándar de Java _no_ ofrece aún ciertos tipos de datos que un programador funcional 
consideraría fundamentales, tales como _Either_, _Try_ o tuplas con componentes anónimos. Existen unas cuantas 
librerías que se ocupan de proveer estas abstracciones (entre las que se destaca 
[vavr](https://www.vavr.io)) pero su adopción ha sido 
limitada pues la mayoría de desarrolladores Java no están familiarizados con la programación funcional.

El siguiente ejemplo ilustra una secuencia funcional de transformaciones que, dado un arreglo de nombres de archivo, 
genera un diccionario de palabras presentes en los archivos dados presentando primero las palabras más empleadas:

```java
public static void main(
        String[] args) {
  final var regexEspacios = 
    Pattern.compile("\\s+");
  final var regexPalabras = 
    Pattern.compile("\\p{IsLatin}+");
  final var omitidas = 
    Set.of("a", "como", "con", "de", "del", "el", "en", "es", "la", 
        "las", "más", "para", "por",  "que", "se", "un", "una", "y");
  // Para cada nombre de archivo...
  Arrays.stream(args)
    // Extrae líneas de c/archivo
    .flatMap(archivo -> {
      // horror: excepciones!!!
      try { 
        return new BufferedReader(
          new FileReader(archivo))
          .lines();
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    })
  // Parte línea en palabras
  .flatMap(linea -> 
    Arrays.stream(
      regexEspacios.split(linea)))
  // Excluye palabras omitidas
  .filter(palabra -> 
    !omitidas.contains(palabra)) 
  // Crea validador regex
  .map(regexPalabras::matcher)
  // Incluye solo alfabéticas
  .filter(Matcher::matches) 
  // Extrae palabra alfabética
  .map(Matcher::group)
  // Uniformiza a minúscula
  .map(String::toLowerCase)
  // Crea mapa palabra/cuenta      
  .collect(groupingBy(
    identity(), counting())) 
  // Ordena #ocurrencias desc
  .entrySet().stream()
  .sorted((e1, e2) -> (int) 
    (e2.getValue() - e1.getValue()))
  // Imprime resultados
  .forEach(System.out::println);
}
```

El uso de funciones de orden superior debería hacer este código suficientemente inteligible. 

> 👉 El tener que ocuparse de la excepción al abrir cada archivo, sin embargo, es un tanto descorazonador y aunque 
existen trucos para evitarla, esta incomodidad pone en evidencia una de las falencias más lamentadas de Java: las 
excepciones chequeadas.

Aplicando esta transformación a este archivo obtenemos como primeras líneas:

```
lenguajes=35
java=30
lenguaje=27
jvm=26
funcionales=18
scala=16
programación=15
capacidades=14
versión=12
tipos=12
```

## Lenguajes Funcionales JVM Exitosos: Scala

Scala es un lenguaje "híbrido" (funcional y orientado a objetos) originalmente concebido por 
[Martin Oderski](https://en.wikipedia.org/wiki/Martin_Odersky)
en 2004 
con un fuerte énfasis en su uso funcional. Aunque otros lenguajes ejercieron influencia en su diseño, las dos 
influencias más claras son Haskell y por supuesto, Java.

Scala se originó en la Escuela Politécnica de Lausana (EPFL, dentro de la cual continúa evolucionando) pero no es un 
lenguaje puramente "académico" y su uso se ha difundido grandemente a nivel mundial siendo empleado por compañías 
tan disímiles como Twitter, Apple, Google, Duolingo o Morgan Stanley.

Martin Oderski, profesor de la EPFL, fue también autor del primer compilador de Java así como diseñador de los tipos 
genéricos de Java (añadidos en la versión 1.5).

Scala se difundió inicialmente como un "mejor Java" en oposición a la verbosidad y rigidez sintáctica de Java. Más 
importantemente, Scala fue pionero de la programación funcional sobre la JVM y se podría afirmar que continúa siéndolo.

La librería estándar de Scala contiene implementaciones de las clases de tipos y tipos de datos funcionales 
"clásicos": _Either_, _Option_, _Try_, enumeraciones GADT, etc. 

Scala ofrece, así mismo, una rica librería de colecciones con amplias capacidades funcionales. Estas colecciones no 
son compatibles (en el sentido 
[Liskov](https://en.wikipedia.org/wiki/Liskov_substitution_principle)) 
con las colecciones de Java aunque existe un mecanismo quasi-transparente de conversión entre las colecciones de los 
dos lenguajes. 

Un potente aspecto sintáctico del lenguaje Scala es el llamado `for` _monádico_ que permite secuenciar operaciones 
monádicas (`map`, `flatMap` y `filter`) con mínima verbosidad y excelente legibilidad.

Empleando este `for` la construcción del diccionario en Scala sería:

```scala
object Diccionario:
  val Blancos = "\\s+".r
  val Palabra = "\\p{IsLatin}+".r

  val Omitidas = Set(
    "a", "como", "con", "de", "del", "el", "en", "es", "la", "las", "más", "para", "por",
    "que", "se", "un", "una", "y")

@main
def imprimir(archivos: String*) =
  // For monádico aquí...
  val palabras = for
    archivo <- archivos
    linea <- Source
      .fromFile(archivo)
      .getLines()
    palabra <-Blancos.split(linea)
    if Palabra.matches(palabra)&&
       !Omitidas.contains(palabra)
  yield palabra.toLowerCase

  // Agrupa, ordena e imprime
  palabras
    .groupBy(identity)
    .toSeq
    .map(p => (p._1, p._2.size))
    .sortBy(-_._2)
    .map(p => s"${p._1}=${p._2}")
    .foreach(println)
```

## Lenguajes Funcionales JVM Exitosos: Kotlin

```kotlin
fun main(args: Array<String>) {
  val Espacios =
    "\\s+".toRegex()
  val Palabra =
    "\\p{IsLatin}+".toRegex()

  val Omitidas = setOf(
    "a", "como", "con", "de", "del", "el", "en", "es", "la", "las", "más", "para", "por",
    "que", "se", "un", "una", "y"
  )

  args
    .flatMap { File(it)
      .bufferedReader()
      .lineSequence() }
    .flatMap { it.split(Espacios) }
    .filter { Palabra.matches(it) 
       && !Omitidas.contains(it) }
    .groupBy { it }
    .mapValues{(_,ps) -> ps.size }
    .toList()
    .sortedBy { -it.second }
    .map { 
      "${it.first}=${it.second}" }
    .forEach(::println)
}
```
## Conclusiones



