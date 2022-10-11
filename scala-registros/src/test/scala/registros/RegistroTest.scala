package registros

import munit.FunSuite

import java.io.{StringReader, StringWriter}
import java.text.DecimalFormat

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
        "nombre" -> "name",
        "apellido" -> "surname",
        "saldo" -> "balance"
      ),
      recolectandoCon(
        registroFijo(24),
        recolectorFijoEnMemoria,
        campoSalidaFijo("name", 0, 8),
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
        campoFijoEntrada("nombre", 0, 8),
        campoFijoEntrada("apellido", 8, 8),
        campoFijoEntrada("saldo", 16, 6, convertidorNumerico("000000", 100))
      ),
      renombrando(
        "nombre" -> "name",
        "apellido" -> "surname",
        "saldo" -> "balance"
      ),
      recolectandoCon(
        dalimitadoSalida(3),
        recolectorDelimitadoEnMemoria(","),
        campoSalidaDelimitado("name", 0),
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
  }



