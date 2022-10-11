package registros

import munit.FunSuite

import java.io.{BufferedReader, InputStreamReader, StringReader, StringWriter}
import java.sql.{Connection, Driver, DriverManager}
import java.text.DecimalFormat
import java.util.Properties

class RegistroTest extends FunSuite :
  test("Consume registros delimitados, produce registros fijos") {
    val registros =
      """
        |janet,doe,1000
        |john,doe,750
        |""".stripMargin

    val resultado = copiar(
      leyendoLineas(registros),
      extrayendoCon(
        dalimitadorEntrada(","),
        campoEntradaDelimitado("nombre", 0),
        campoEntradaDelimitado("apellido", 1),
        campoEntradaDelimitado("saldo", 2, _.toDouble),
      ),
      renombrando(
        "nombre" -> "nombre",
        "apellido" -> "surname",
        "saldo" -> "balance"
      ),
      recolectandoCon(
        registroFijo(24),
        recolectorFijoEnMemoria,
        campoSalidaFijo("nombre", 0, 8),
        campoSalidaFijo("surname", 8, 8),
        campoSalidaFijo("balance", 16, 6, formatoNumerico("000000", 100))
      )
    )

    val resultadoEsperado = List(
      "janet   doe     100000  ",
      "john    doe     075000  ",
    )
      .mkString

    assert(resultado == resultadoEsperado)
  }

  test("Consume registros fijos, produce registros delimitados") {

  val registros = List(
      "janet   doe     100000  ",
      "john    doe     075000  ",
    )
      .mkString

    val resultado = copiar(
      leyendoArchivoFijo(24, StringReader(registros)),
      extrayendoCon(
        campoEntradaFijo("nombre", 0, 8),
        campoEntradaFijo("apellido", 8, 8),
        campoEntradaFijo("saldo", 16, 6, convertidorNumerico("000000", 100))
      ),
      renombrando(
        "nombre" -> "nombre",
        "apellido" -> "surname",
        "saldo" -> "balance"
      ),
      recolectandoCon(
        dalimitadoSalida(3),
        recolectorDelimitadoEnMemoria(","),
        campoSalidaDelimitado("nombre", 0),
        campoSalidaDelimitado("surname", 1),
        campoSalidaDelimitado("balance", 2, _.toString),
      )
    )

    val resultadoEsperado =
      """
        |janet,doe,1000
        |john,doe,750
        |""".stripMargin

    assert(resultado.trim == resultadoEsperado.trim)
  }

  test("Consume y produce registros de base de datos") {
    val conexion1 = conexion("test1",
      """
        |CREATE TABLE entrada(id VARCHAR(1), nombre VARCHAR(16));
        |INSERT INTO entrada(id, nombre) VALUES('1', 'janet doe');
        |INSERT INTO entrada(id, nombre) VALUES('2', 'john doe');
        |""".stripMargin
    )
    val conexion2 = conexion("test2",
      "CREATE TABLE salida(id VARCHAR(1), nombre VARCHAR(16));")

    val resultado = copiar(
      leyendoSql(conexion1, Map.empty,
        """
          |SELECT upper(id) id,
          |       upper(nombre) nombre
          |FROM entrada""".stripMargin),
      extrayendoSql(
        campoEntradaDB("id"),
        campoEntradaDB("nombre")
      ),
      identity,
      insertandoTabla(Tabla("salida", nombresColumna = "id", "nombre"), 2)
                     (conexion2, Map.empty)
    )
    assert(resultado == 2)

    val sql = "SELECT * FROM salida";
    val mapaResultado =
      rs2Iterator(conexion2.createStatement().executeQuery(sql))
        .map(rs2Map)
        .toSet
    assert(mapaResultado == Set(
      Map("id" -> "1", "nombre" -> "JANET DOE"),
      Map("id" -> "2", "nombre" -> "JOHN DOE"),
    ))
  }

  def conexion(nombreBaseDatos: String, sql: String): Connection = {
    Class.forName("org.h2.Driver")
    val conexion = DriverManager.getConnection(
      s"jdbc:h2:mem:$nombreBaseDatos;DATABASE_TO_LOWER=true;DB_CLOSE_ON_EXIT=false",
      Properties()
    )
    sql.
      split(";\\n")
      .map(_.trim)
      .filterNot(_.isEmpty)
      .foreach(conexion.createStatement().executeUpdate)
    conexion
  }

  def readResource(nombreRecurso: String): String =
    val is = Thread.currentThread().getContextClassLoader.getResourceAsStream(nombreRecurso)
    require(is != null, s"No existe recurso: $nombreRecurso")
    val sb = StringBuffer()
    val buffer = Array.ofDim[Char](4096)
    val lector = BufferedReader(InputStreamReader(is))
    Iterator.continually(lector.read(buffer))
      .takeWhile(_ > 0)
      .foreach(cnt => sb.append(buffer, 0, cnt))
    sb.toString




