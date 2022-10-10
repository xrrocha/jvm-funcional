package registros

class CampoFijoEntrada[S](nombre: String,
                          posicion: Int,
                          longitud: Int,
                          extraer: String => S)
  extends CampoEntrada[Array[Char], S](nombre, a => extraer(String(a, posicion, longitud)))

class CampoFijoSalida[E](nombre: String,
                         posicion: Int,
                         longitud: Int,
                         formatear: E => String = (e: E) => e.toString)
  extends CampoSalida[Array[Char]](
    nombre, (registroEntrada, registroSalida) => {
      // TODO Adicionar justificación y relleno de campos fijos de salida
      val valor = formatear(registroEntrada(nombre).asInstanceOf[E]).toCharArray
      System.arraycopy(valor, 0, registroSalida, posicion, math.min(valor.length, longitud))
    })

def campoFijo(nombre: String, posicion: Int, longitud: Int): CampoFijoSalida[String] =
  CampoFijoSalida(nombre, posicion, longitud)

def campoFijo[E](nombre: String, posicion: Int, longitud: Int, formatear: E => String): CampoFijoSalida[E] =
  CampoFijoSalida(nombre, posicion, longitud, formatear)
