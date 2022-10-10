package registros

import munit.FunSuite

class TransRedTest extends FunSuite:

  test("Genera, transforma y reduce") {

    val transRed =
      TransRed(Iterator("uno", "dos", "tres"), _.toUpperCase, _.toList)

    assert(
      List("UNO", "DOS", "TRES") == transRed()
    )
  }



