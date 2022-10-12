package registros

import java.io.*
import java.text.DecimalFormat
import scala.util.matching.Regex

def leyendoLineas(lector: Reader): Iterator[String] = new Iterator[String] :
  private val lectorLinea = BufferedReader(lector)
  private var linea = lectorLinea.readLine()

  override def hasNext: Boolean = linea != null

  override def next(): String =
    val lineaAnterior = linea
    linea = lectorLinea.readLine()
    lineaAnterior
def leyendoLineas(is: InputStream): Iterator[String] =
  leyendoLineas(InputStreamReader(is))
def leyendoLineas(lineas: String): Iterator[String] =
  leyendoLineas(StringReader(lineas.trim))

def separarPorDelimitador(cadena: String, delimitador: Regex): IndexedSeq[String] =
  delimitador.split(cadena).toIndexedSeq

def separarPorDelimitador(cadena: String, delimitador: String): IndexedSeq[String] =
  separarPorDelimitador(cadena, delimitador.r)

def delimitadorEntrada(delimitador: String): String => IndexedSeq[String] =
  separarPorDelimitador(_, delimitador)

def dalimitadoSalida(numeroCampos: Int): () => collection.mutable.IndexedSeq[String] =
  () => collection.mutable.IndexedSeq.fill(numeroCampos)("")

class CampoEntradaDelimitado[S](nombre: String,
                                posicion: Int,
                                extraer: String => S)
  extends CampoEntrada[IndexedSeq[String], S](nombre, is => extraer(is(posicion)))

def campoEntradaDelimitado(nombre: String, posicion: Int): CampoEntradaDelimitado[String] =
  CampoEntradaDelimitado(nombre, posicion, identity)

def campoEntradaDelimitado[S](nombre: String,
                              posicion: Int,
                              extraer: String => S): CampoEntradaDelimitado[S] =
  CampoEntradaDelimitado(nombre, posicion, extraer)

class CampoSalidaDelimitado[E](nombre: String,
                               posicion: Int,
                               formatear: E => String)
  extends CampoSalida[collection.mutable.IndexedSeq[String]](nombre, (registro, campos) => {
    campos(posicion) = formatear(registro(nombre).asInstanceOf[E])
  })

def campoSalidaDelimitado[E](nombre: String, posicion: Int, formatear: E => String) =
  CampoSalidaDelimitado(nombre, posicion, formatear)
def campoSalidaDelimitado(nombre: String, posicion: Int) =
  CampoSalidaDelimitado[String](nombre, posicion, identity)

def recolectorDelimitadoEnMemoria(delimitador: String) = new Recolector[collection.mutable.IndexedSeq[String], String] {
  private val escritor = StringWriter()

  override def acumular(item: collection.mutable.IndexedSeq[String]): Unit =
    escritor.write(item.mkString(delimitador) + "\n")

  override def completar: String =
    escritor.toString
}
