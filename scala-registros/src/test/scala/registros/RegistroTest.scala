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

    val resultado = copiar(
      leyendoLineas(registros),
      extrayendoCon(
        delimitador(","),
        campo("nombre", 0),
        campo("apellido", 1),
        campo("saldo", 2, _.toDouble),
      ),
      renombrando(
        "nombre" -> "name",
        "apellido" -> "surname",
        "saldo" -> "balance"
      ),
      reduciendoALista
    )

    assert(resultado == List(
      Map("name" -> "janet", "surname" -> "doe", "balance" -> 1000.0),
      Map("name" -> "john", "surname" -> "doe", "balance" -> 750.0),
    ))
  }



