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

def lectorRs(rs: ResultSet): Iterator[Map[String, _]] = new Iterator[Map[String, _]] {
  override def hasNext: Boolean = rs.next()

  override def next(): Map[String, _] = mapaRs(rs)
}

def dividirConDelimitador(cadena: String, delimitador: Regex): IndexedSeq[String] =
  delimitador.split(cadena).toIndexedSeq
def dividirConDelimitador(cadena: String, delimitador: String): IndexedSeq[String] =
  dividirConDelimitador(cadena, delimitador.r)

class Campo[E, S](val nombre: String,
                  val extraer: E => S,
                  val escribir: S => String = (s: S) => s.toString)

class CampoDelimitado[S](nombre: String,
                         indice: Int,
                         extraer: String => S,
                         escribir: S => String = (s: S) => s.toString)
  extends Campo[IndexedSeq[String], S](nombre, is => extraer(is(indice)), escribir)

def campoDelimitado(nombre: String, indice: Int): CampoDelimitado[String] =
  CampoDelimitado(nombre, indice, identity, identity)

def campoDelimitado[S](nombre: String,
                       indice: Int,
                       extraer: String => S,
                       escribir: S => String = (s: S) => s.toString): CampoDelimitado[S] =
  CampoDelimitado(nombre, indice, extraer, escribir)

class CampoFijo[S](nombre: String,
                   posicion: Int,
                   longitud: Int,
                   extraer: String => S,
                   escribir: S => String = (s: S) => s.toString)
  extends Campo[Array[Char], S](nombre, a => extraer(String(a, posicion, longitud)))

def constructorMapa[E](campos: List[Campo[E, _]]): E => Map[String, _] =
  constructorMapa(identity, campos)
def constructorMapa[I, E](dividir: I => E,
                          campos: List[Campo[E, _]]): I => Map[String, _] =
  input =>
    val valoresCampo = dividir(input)
    campos.map(campo => (campo.nombre, campo.extraer(valoresCampo))).toMap

def renombradorMapa(nombres: List[(String, String)]): Map[String, _] => Map[String, _] =
  mapa =>
    nombres
      .map { case (nombreEntrada, nombreSalida) => (nombreSalida, mapa(nombreEntrada)) }
      .toMap


def transformarReducir[E, S, O](lector: => Iterator[E],
                                transformar: E => S,
                                reducir: Iterator[S] => O): O =
  reducir(lector.map(transformar))