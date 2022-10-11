package registros

import java.io.{BufferedReader, Reader, StringWriter, Writer}

class CampoEntradaFijo[S](nombre: String,
                          posicion: Int,
                          longitud: Int,
                          convertir: String => S)
  extends CampoEntrada[Array[Char], S](
    nombre,
    a => convertir(String(a, posicion, longitud).trim))

def campoEntradaFijo[E](nombre: String, posicion: Int, longitud: Int, convertir: String => E) =
  CampoEntradaFijo(nombre, posicion, longitud, convertir)
def campoEntradaFijo(nombre: String, posicion: Int, longitud: Int) =
  CampoEntradaFijo(nombre, posicion, longitud, identity)

def leyendoArchivoFijo(longitud: Int, lector: Reader) = new Iterator[Array[Char]] :
  private val lectorRegistro = BufferedReader(lector)
  private val buffer = new Array[Char](longitud)
  private var caracteresLeidos = lectorRegistro.read(buffer)

  override def hasNext: Boolean = caracteresLeidos >= 0

  override def next(): Array[Char] =
    // TODO Copiar buffer más eficientemente
    val cadena = String(buffer, 0, caracteresLeidos).toCharArray
    caracteresLeidos = lectorRegistro.read(buffer)
    cadena

class CampoSalidaFijo[E](nombre: String,
                         posicion: Int,
                         longitud: Int,
                         formatear: E => String = (e: E) => e.toString)
  extends CampoSalida[Array[Char]](
    nombre, (registroEntrada, registroSalida) => {
      // TODO Adicionar justificación y relleno de campos fijos de salida
      val valor = formatear(registroEntrada(nombre).asInstanceOf[E]).toCharArray
      System.arraycopy(valor, 0, registroSalida, posicion, math.min(valor.length, longitud))
    })

def campoSalidaFijo(nombre: String, posicion: Int, longitud: Int): CampoSalidaFijo[String] =
  CampoSalidaFijo(nombre, posicion, longitud)

def campoSalidaFijo[E](nombre: String, posicion: Int, longitud: Int, formatear: E => String): CampoSalidaFijo[E] =
  CampoSalidaFijo(nombre, posicion, longitud, formatear)

def registroFijo(longitud: Int): () => Array[Char] =
  () => Array.fill[Char](longitud)(' ')

def recolectorFijoEnMemoria = new Recolector[Array[Char], String] {
  private val escritor = StringWriter()

  override def acumular(item: Array[Char]): Unit =
    escritor.write(String(item))

  override def completar: String =
    escritor.toString
}
