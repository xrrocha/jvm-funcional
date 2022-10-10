# Scott: Un Ejemplo de DSL Funcional en Java

![tl-dr;](docs/img/tl-dr.png)
Este repositorio ilustra el diseño, implementación y uso de un lenguaje de dominio específico (DSL) en Java 17
empleando patrones funcionales.
El argumento de estudio es una aplicación SpringBoot JPA inspirada en el tradicional esquema Oracle _scott/tiger_.
Para comprender las técnicas empleadas para implementar este DSL es útil tener familiaridad con las 
[lambdas de Java](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) 
así como con 
[Spring Data JPA](https://spring.io/projects/spring-data-jpa).

El DSL implementado en este repositorio captura patrones repetitivos en componentes Spring que hacen uso de 
repositorios JPA (`@Service`, `@Controller`, ...).

Un servicio Spring típico implementaría imperativamente la persistencia de una nueva instancia de `Departamento` en 
la base de datos como:

```java
// Retorna id generado para nuevo departamento
public String crearDepartamento(String codigo, String nombre, String localidad) {
    // Construye y valida departamento
    final Departamento departamento;
    try {
        departamento = Departamento.builder()
                            .codigo(codigo)
                            .nombre(nombre)
                            .localidad(localidad)
                            .build();
    } catch (Exception e) {
        throw new RuntimeException("Error de validación creando departamento", e);
    }

    // Persiste nuevo departamento
    final Departamento departamentoGuardado;
    try {
        departamentoGuardado = repositorioDepartamento.save(departamento);
    } catch (Exception e) {
        throw new RuntimeException("Error persistiendo nuevo departamento", e);
    }

    // Retorna id generado para nuevo departamento
    return departamentoGuardado.getId();
}
```

Empleando el DSL implementado en este repositorio, el código anterior queda reducido a:

```java
// Retorna id generado para nuevo departamento
public String crearDepartamento(String codigo, String nombre, String localidad) {
    return persistirInstancia(
        repositorioDepartamento, Departamento::getId,
        () -> Departamento.builder()
                  .codigo(codigo)
                  .nombre(nombre)
                  .localidad(localidad)
                  .build()
    ));
}
```

### El Modelo de Datos _scott/tiger_

El modelo de datos de ejemplo esta inspirado en el esquema 
[scott/tiger](https://www.orafaq.com/wiki/SCOTT) tradicionalmente empleado por Oracle 
Corporation para enseñar el lenguaje SQL. 

Además de reformularlo en español, en este repositorio se le añade a este modelo algunos pequeños detalles para 
utilizarlo mediante JPA:

![Modelo](docs/img/modelo.png)

La definición de `Departamento` como entidad JPA mostrada a continuación hace uso de 
[Lombok](https://www.javatpoint.com/lombok-java) y de 
[Java EE Validation](https://docs.oracle.com/javaee/7/tutorial/bean-validation.htm)
así como del soporte brindado por la superclase 
[Entidad](src/main/java/scott/infra/jpa/entidad/Entidad.java):

```java
@Entity
@Table(name = "departamento", uniqueConstraints = { 
    @UniqueConstraint(name = "dept_uk_codigo", columnNames = {"codigo"})})
@Getter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Departamento extends Entidad {
    @ToString.Include
    @NotNull(message = "El código del departamento debe ser especificado")
    @Pattern(regexp = "^[0-9]{2}$", message = "Código de departamento inválido; debe constar de dos dígitos")
    @Basic(optional = false)
    @Column(name = "codigo", nullable = false, length = 2)
    private String codigo;

    @ToString.Include
    @NotNull(message = "El nombre del departamento debe ser especificado")
    @Pattern(regexp = "^\\p{IsLatin}{2,16}$", message = "Nombre de departamento inválido; solo puede contener letras")
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 16)
    private String nombre;

    @ToString.Include
    @NotNull(message = "La localidad del departamento debe ser especificada")
    @Pattern(regexp = "^\\p{IsLatin}{2,16}$", message = "Localidad de departamento inválida; solo puede contener letras")
    @Basic(optional = false)
    @Column(name = "localidad", nullable = false, length = 16)
    private String localidad;

    @OneToMany(mappedBy = "departamento", cascade = CascadeType.ALL)
    private final Set<Empleado> empleados = new HashSet<>();

    @Builder
    public Departamento(String codigo, String nombre, String localidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.localidad = localidad;
        validarAtributos();
    }

    public String relocalizar(String nuevaLocalidad) {
        String anteriorLocalidad = this.localidad;
        this.localidad = nuevaLocalidad;
        validarAtributos();
        return anteriorLocalidad;
    }
}
```
👉 Este repositorio ilustra también otros aspectos de implementación de JPA que pueden ser de interés para el is 
más allá del DSL. Esto incluye soporte genérico a enumeraciones, generación de identificadores desde la aplicación, 
envoltura de repositorios JPA en español y otros temas más...


### Insertando una Nueva Instancia de Entidad (Toma 1)

Para persistir una nueva instancia de `Departamento` se requeriría algo como:

```java
public String crearDepartamento(String codigo, String nombre, String localidad) {
    // Construye y valida departamento
    final Departamento departamento;
    try {
        departamento = Departamento.builder()
                .codigo(codigo)
                .nombre(nombre)
                .localidad(localidad)
                .build();
    } catch (Exception e) {
        throw new RuntimeException("Error de validación creando departamento", e);
    }

    // Persiste nuevo departamento
    final Departamento departamentoGuardado;
    try {
        departamentoGuardado = repositorioDepartamento.save(departamento);
    } catch (Exception e) {
        throw new RuntimeException("Error de persistencia creando departamento", e);
    }

    // Retorna id generado para nuevo departamento
    return departamentoGuardado.getId();
}
```

Para persistir una nueva instancia de `Empleado` se requeriría algo _muy semejante_, en el estilo de:

```java
public String crearEmpleado(String codigo, String nombre, Genero genero) {
    // Construye y valida instancia de empleado
    final Empleado empleado;
    try {
        empleado = Empleado.builder()
            .codigo(codigo)
            .nombre(nombre)
            .genero(genero)
            .build();
    } catch (Exception e) {
        throw new RuntimeException("Error de validación creando empleado", e);
    }

    // Persiste nuevo Empleado
    final Empleado empleadoGuardado;
    try {
        empleadoGuardado = repositorioEmpleado.save(empleado);
    } catch (Exception e) {
        throw new RuntimeException("Error de persistencia creando empleado", e);
    }

    // Retorna id generado para nuevo empleado
    return empleadoGuardado.getId();
}
```

En los dos casos se repite el mismo patrón:

- Declarar y poblar una nueva instancia de la entidad, generando una excepción si hay errores de validación
- Guardar la nueva instancia creando así una nueva versión ya almacenada y generando una excepción si hay errores 
  de persistencia
- Retornar la nueva clave primaria generada por el sistema

Varían los detalles, pero el código (repetitivo y tedioso) tiene siempre la misma estructura!

### Claves Naturales y Sintéticas

En el uso de bases de datos relacionales de hoy es frecuente reemplazar las claves primarias "naturales" (tales como 
la _cédula_ de la persona o el _código_ del departamento) por claves primarias "sintéticas" generadas por el sistema.

```sql
CREATE TABLE departamento (
    id     INTEGER     NOT NULL DEFAULT nextval('departamento_seq') PRIMARY KEY,
    codigo VARCHAR(16) NOT NULL UNIQUE,
    nombre VARCHAR(24) NOT NULL
);
CREATE TABLE empleado (
    id              VARCHAR(32) NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo          VARCHAR(16) NOT NULL UNIQUE,
    nombre          VARCHAR(24) NOT NULL,
    id_departamento INTEGER     NOT NULL REFERENCES departamento (id),
    id_supervisor   VARCHAR(32) REFERENCES empleado (id)
);
```

Para impedir que se añadan nuevas instancias con valores duplicados de clave natural:

- Se añade una restricción `UNIQUE` en la(s) columna(s) de la clave natural
- Se añade una anotación `@Table/@UniqueConstraint` a la entidad JPA 
- Se verifica en la aplicación Spring que no exista ya en la tabla una fila con el mismo valor de clave natural

👉 En nuestro repositorio de ejemplo hemos establecido la simplificación de que todas las claves primarias sintéticas
son de tipo `String` y corresponden a un _random `UUID`_ generado desde la aplicación.

### Insertando una Nueva Instancia de Entidad (Toma 2)

Para garantizar que no haya múltiples departamentos con el mismo código, la persistencia de una nueva instancia de 
`Departamento` luciría ahora como:

```java
public String crearDepartamento(String codigo, String nombre, String localidad) {
    // Construye y valida departamento
    final Departamento departamento;
    try {
        departamento = Departamento.builder()
            .codigo(codigo)
            .nombre(nombre)
            .localidad(localidad)
            .build();
    } catch (Exception e) {
        throw new RuntimeException("Error de validación creando departamento", e);
    }
    
    // *** La nueva validación de unicidad ocurre aquí ***
    // Valida que el código de departamento no sea duplicado
    final Optional<Departamento> optDepartamento;
    try {
        optDepartamento = repositorioDepartamento .findByCodigo(codigo);
    } catch (Exception e) {
        throw new RuntimeException("Error recuperando departamento por código", e);
    }
    optDepartamento.ifPresent(d -> {
        String mensaje = "Ya existe un departamento con codigo %s: %s!".formatted(codigo, d.getNombre());
        throw new IllegalArgumentException(mensaje);
    });
    // *** Fin de nueva validación de unicidad ***

    // Persiste nuevo departamento
    final Departamento departamentoGuardado;
    try {
        departamentoGuardado = repositorioDepartamento.save(departamento);
    } catch (Exception e) {
        throw new RuntimeException("Error de persistencia creando departamento", e);
    }

    // Retorna id generado para nuevo departamento
    return departamentoGuardado.getId();
}
```

La creación de una nueva entidad de `Empleado` se verá también aumentada con una verificación adicional equivalente.

Esto es repetitivo, tedioso y _propenso al error_!

👉 **Una de las principales fuentes de _bugs_ en el desarrollo de aplicaciones son los errores en la transcripción de 
recetas repetitivas como esta**.

### Capturando Recetas Repetitivas

Qué es lo que cambia de entidad en entidad cuando queremos persistir una nueva instancia en la base de datos?

- Cambia el tipo de datos concreto de la entidad (`Departamento`, `Empleado`, ...)
- Cambia la lógica que construye y valida una nueva instancia de la entidad en memoria
- Cambia la lógica que valida la instancia antes de persistirla (por ejemplo para validar unicidad de clave natural)

Todo lo demás tiene _siempre_ la misma lógica!

Para formular las partes móviles de forma reutilizable Java provee dos poderosos mecanismos: 

- Tipos de datos genéricos y 
- Lambdas

Para las clases de entidad se puede definir un tipo genérico `E`.

La porción de lógica que construye en memoria una nueva instancia de entidad es una lambda de tipo `Supplier<E>`.

La porción de lógica que valida la nueva instancia de entidad en memoria antes de persistirla sería un `Consumer<E>` 
opcional que puede fallar con una excepción.

Veamos:

```java
public static<E, I> I persistirInstancia(
    JpaRepository<E, I>   repositorio,
    Function<E, I>        clavePrimaria,
    Consumer<E>           validacion,
    Supplier<E>           crearInstancia
) {
    final E entidad;
    try {
        entidad = crearInstancia.get();
    } catch(Exception e) {
        throw new ExcepcionDSL("Error creando instancia de entidad en memoria",e);
    }

    if (validacion != null) {
        try {
            validacion.accept(entidad);
        } catch(Exception e) {
            throw new ExcepcionDSL("Error de validación de entidad",e);
        }
    }

    final E entidadGuardada;
    try {
        entidadGuardada = repositorio.save(entidad);
    } catch(Exception e) {
        throw new ExcepcionDSL("Error persistiendo nueva instancia",e);
    }

    return clavePrimaria.apply(entidadGuardada);
}

// Este método sintetiza y retorna una nueva función (high-order)
public static <E, C> Consumer<E> detectarDuplicado(Function<C, Optional<E>> extractor, C valorClave) {
    return e -> extractor.apply(valorClave).ifPresent(t -> {
        throw new ExcepcionDSL("Ya existe una instancia con la misma clave: %s".formatted(valorClave));
    });
}
```

Armados con estos método genérico, la creación de un nuevo departamento luciría como:

```java
public String crearDepartamento(String codigo, String nombre, String localidad) {
    return persistirInstancia(
        repositorioDepartamento,
        detectarDuplicado(repositorioDepartamento::buscarPorCodigo, codigo),
        () -> Departamento.builder()
                .codigo(codigo)
                .nombre(nombre)
                .localidad(localidad)
                .build()
    ));
}
```

🤩 Aah, _excelente_ simplificación! 

Y es segura en tipos de datos! Si, por error, escribiéramos `repositorioEmpleado` donde debiera decir 
`repositorioDepartamento`, el compilador de Java y/o la IDE detectarían la discrepancia y se quejarían _de inmediato_.

### Reflexiones Acerca del Estilo del DSL

Como es natural, nuestro método DSL es, en su forma inicial, imperfecto:

- No soluciona _todos_ nuestros problemas
- Nos trae _nuevos_ problemas causados por él mismo

Qué problemas nuevos nos trae? 

Uno inmediatamente evidente es que los mensajes de error son demasiado genéricos y no proveen contexto. Donde 
nuestra versión original solía decir `Ya existe un departamento con codigo 30: Ventas!` ahora nuestro método DSL 
reporta un críptico `Ya existe una instancia con la misma clave: 30`. Claramente, esto podría 
mejorar!

Qué problemas no soluciona?

Un problema con nuestro código original es que hace uso muy liberal de las excepciones. Sería deseable que nuestro 
DSL nos liberara de tener que lidiar continuamente con las excepciones, pero también que nos permitiera ocuparnos 
apropiadamente de ellas cuando así se requiera.

👉 Algunos programadores Java no verían en esto un problema. Después de todo, las excepciones son el mecanismo estándar 
del lenguaje para reportar o propagar condiciones de error. Sin embargo, las excepciones rompen el control de flujo
y, tomadas a la ligera, dificultan lidiar con las condiciones de error. En la práctica, muchos desarrolladores 
simplemente ignoran las excepciones y las dejan propagar hasta el nivel superior de la aplicación! 
_Somewhere in the Rytridian Galaxy, Ultra Lord weeps 🥺_

### El Tipo de Datos `Either` al Rescate!

La programación funcional ofrece también una manera de ocuparse de las condiciones de error _como datos_ y no como una 
ruptura del flujo natural del programa: el tipo de datos `Either`

La librería funcional [Vavr](https://vavr.io) provee una implementación conveniente del tipo de datos funcional 
`Either<L, R>` para Java. 

Una instancia de `Either<L, R>` contiene uno de dos posibles valores:

- Un valor útil (`R`, por _right_) si la computación que le dió origen completó exitosamente, o
- Un valor de error (`L`, por _left_) si la computación terminó anormalmente

👉 Que el valor exitoso de `Either` esté a la derecha y no a la izquierda puede resultar contra-intuitivo a algunos
pero es, simplemente, una convención (originalmente establecida por el lenguaje Haskell).

Lo interesante del uso de este tipo de datos es que, cuando todos los métodos coinciden en retornar `Either`, es 
posible encadenarlos en _pipelines_ de transformación que parecerían no tener que ocuparse de posibles errores!
Esto produce código muy legible con apariencia de _happy path_.

Cómo? Primero necesitamos una forma de convertir lambdas que generan excepciones en instancias de `Either`.

Es fácil convertir una lambda que retorna `T` (y que puede fallar) en un `Either<RuntimeException, T>` tal que la
excepción retornada en el lado izquierdo contenga un mensaje apropiado para el contexto de ejecución:

```java
public static <T> 
Either<RuntimeException, T> eitherCatch(String contexto, CheckedFunction0<T> lambda) {
    try {
        return Either.right(lambda.apply());
    } catch (Throwable t) {
        return Either.left(new RuntimeException("Error: " + contexto, t));
    }
}
```

Dado este método de conversión, la lógica de persistencia de una nueva entidad se simplificaría como:

```java
public static <E, I> Either<Falla, I> persistirInstancia(
    JpaRepository<E, I>     repositorio,
    CheckedFunction1<E, I>  clavePrimaria,
    CheckedConsumer<E>      validacion,
    CheckedFunction0<E>     crearInstancia
) {
    return eitherCatch("creando instancia de entidad en memoria", crearInstancia)
        .flatMap(entidad ->
            eitherCatch("validando instancia de entidad en memoria", entidad, validacion))
        .flatMap(entidad ->
            eitherCatch("persistiendo nueva instancia", () -> repositorio.save(entidad)))
        .flatMap(entidad ->
            eitherCatch("recuperando clave primaria", () -> clavePrimaria.apply(entidad)));
}
```

Esta implementación del método DSL es mucho más simple e inteligible que la versión basada en excepciones!

Cuando `Either` falla, la línea de transformación se interrumpe inmediatamente! Por esta razón se dice que el lado 
izquierdo de `Either` causa un _cortocircuito_.

Esta es la razón por la que es posible concatenar las acciones sin (aparentemente) ocuparse de los errores. En el 
código anterior, el texto descriptivo de cada paso de la línea de transformación se utiliza como contexto para 
generar el mensaje de error apropiado para toda posible excepción.

Al final, el nivel superior de la aplicación decide qué hacer cuando hay errores: hacer _logging_, retornar un 
código HTTP 404, etc.

Para apreciar el uso de patrones funcionales en un DSL Java véase el
[DSL de repositorios JPA](src/main/java/scott/infra/jpa/RepositorioDSL.java).

Para apreciar el uso del DSL en código "real" véase:

- [El servicio de departamento](src/main/java/scott/dominio/ServicioDepartamento.java)
- [El servicio de empleado](src/main/java/scott/dominio/ServicioEmpleado.java)
- [La prueba de integración de estos servicios](src/test/java/scott/dominio/EscenarioIT.java)
