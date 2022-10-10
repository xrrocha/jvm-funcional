package registros

import munit.FunSuite

import java.io.StringReader

class RegistroTest extends FunSuite :
  test("Procesa registros delimitados") {
    val registros =
      """
        |janet,doe,1000
        |john,doe,750
        |""".stripMargin
    val lector = lectorLinea(StringReader(registros.trim))

    val transformar = constructorMapa[String, IndexedSeq[String]](
      dividirConDelimitador(_, ","),
      List(
        campoDelimitado("nombre", 0),
        campoDelimitado("apellido", 1),
        campoDelimitado("saldo", 2, _.toDouble),
      )
    )

    val reducir: Iterator[Map[String, _]] => Unit =
      i => println(i.mkString(";"))

    transformarReducir(lector, transformar, reducir)
  }



