package registros

import munit.FunSuite

import java.io.{StringReader, StringWriter}
import java.text.DecimalFormat

class RegistroTest extends FunSuite :
  test("Procesa registros delimitados") {
    val registros =
      """
        |janet,doe,1000
        |john,doe,750
        |""".stripMargin

    val formato = DecimalFormat("000000")
    val formatear = (valor: Double) => formato.format(valor * 100)

    val resultado = copiar(
      leyendoLineas(registros),
      extrayendoCon(
        dalimitadorEntrada(","),
        campoDelimitado("nombre", 0),
        campoDelimitado("apellido", 1),
        campoDelimitado("saldo", 2, _.toDouble),
      ),
      renombrando(
        "nombre" -> "name",
        "apellido" -> "surname",
        "saldo" -> "balance"
      ),
      recolectandoCon(
        () => Array.fill[Char](24)(' '),
        new Recolector[Array[Char], String] {
          private val buffer = StringWriter()
          override def acumular(item: Array[Char]): Unit =
            buffer.write(String(item))

          override def completar: String =
            buffer.toString
        },
        campoFijo("name", 0, 8),
        campoFijo("surname", 8, 8),
        campoFijo("balance", 16, 6, formatear)
      )
    )

    val resultadoEsperado = List(
      "janet   doe     100000  ",
      "john    doe     075000  ",
    )
      .mkString

    assert(resultado == resultadoEsperado)
  }



