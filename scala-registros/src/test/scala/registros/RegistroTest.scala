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
    val items = lectorLinea(StringReader(registros.trim))

    val transformar = constructorMapa(
      dividir(_, ","),
      List(
        campo("nombre", 0),
        campo("apellido", 1),
        campo("saldo", 2, _.toInt),
      )
    )

    val reducir: Iterator[Map[String, _]] => Unit =
      i => println(i.mkString(";"))

    transformarReducir(items, transformar, reducir)
  }



