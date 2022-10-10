package registros

import java.io.{BufferedReader, InputStream, InputStreamReader, Reader}
import java.sql.ResultSet
import scala.util.matching.Regex

def lectorLinea(lector: Reader): Iterator[String] = new Iterator[String] :
  private val lectorLinea = BufferedReader(lector)
  private var linea = lectorLinea.readLine()

  override def hasNext: Boolean = linea != null

  override def next(): String =
    val lineaAnterior = linea
    linea = lectorLinea.readLine()
    lineaAnterior
def lectorLinea(is: InputStream): Iterator[String] =
  lectorLinea(InputStreamReader(is))

def lectorFijo(longitud: Int, lector: Reader) = new Iterator[String] :
  private val lectorRegistro = BufferedReader(lector)
  private val buffer = new Array[Char](longitud)
  private var caracteresLeidos = lectorRegistro.read(buffer)

  override def hasNext: Boolean = caracteresLeidos >= 0

  override def next(): String =
    val cadena = String(buffer, 0, caracteresLeidos)
    caracteresLeidos = lectorRegistro.read(buffer)
    cadena

def mapaRs(rs: ResultSet): Map[String, _] = {
  for i <- 1 to rs.getMetaData.getColumnCount
    yield (rs.getMetaData.getColumnLabel(i), rs.getObject(i))
}
  .toMap

def iteradorRs(rs: ResultSet): Iterator[Map[String, _]] = new Iterator[Map[String, _]] {
  override def hasNext: Boolean = rs.next()

  override def next(): Map[String, _] = mapaRs(rs)
}

def dividir(cadena: String, delimitador: Regex): IndexedSeq[String] =
  delimitador.split(cadena).toIndexedSeq
def dividir(cadena: String, delimitador: String): IndexedSeq[String] =
  dividir(cadena, delimitador.r)

trait DivisionFija(posicion: Int, longitud: Int):
  def extraer(cadena: String): String = cadena.substring(posicion, posicion + longitud)

def dividir(cadena: String, divisiones: Iterable[DivisionFija]): IndexedSeq[String] =
  divisiones.map(_.extraer(cadena)).toIndexedSeq

class Campo[E, S](val nombre: String, val indice: Int, val extraer: E => S)

def campo(nombre: String, indice: Int): Campo[String, String] = Campo(nombre, indice, identity)
def campo[S](nombre: String, indice: Int, extraer: String => S): Campo[String, S] =
  Campo[String, S](nombre, indice, extraer)

def constructorMapa(dividir: String => IndexedSeq[String],
                    campos: List[Campo[String, _]]): String => Map[String, _] =
  cadena =>
    val valores = dividir(cadena)
    campos.map(campo => (campo.nombre, campo.extraer(valores(campo.indice)))).toMap

def renombradorMapa(nombres: List[(String, String)]): Map[String, _] => Map[String, _] =
  mapa =>
    nombres
      .map { case (nombreEntrada, nombreSalida) => (nombreSalida, mapa(nombreEntrada)) }
      .toMap

def transformarReducir[E, S, O](items: => Iterator[E],
                                transformar: E => S,
                                reducir: Iterator[S] => O): O =
  reducir(items.map(transformar))