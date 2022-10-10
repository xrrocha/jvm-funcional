package registros

import java.io.*
import java.sql.{Connection, ResultSet}
import scala.util.matching.Regex
import scala.util.{Try, Using}

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

def lectorFijo(longitud: Int, lector: Reader) = new Iterator[String] :
  private val lectorRegistro = BufferedReader(lector)
  private val buffer = new Array[Char](longitud)
  private var caracteresLeidos = lectorRegistro.read(buffer)

  override def hasNext: Boolean = caracteresLeidos >= 0

  override def next(): String =
    val cadena = String(buffer, 0, caracteresLeidos)
    caracteresLeidos = lectorRegistro.read(buffer)
    cadena

trait Campo(val nombre: String)

class CampoEntrada[E, S](nombre: String, val extraer: E => S) extends Campo(nombre)

def extrayendoCon[I, E](separar: I => E,
                        campos: CampoEntrada[E, _]*): I => Map[String, _] =
  input =>
    val valoresCampo = separar(input)
    campos.map(campo => (campo.nombre, campo.extraer(valoresCampo))).toMap

def extrayendoCon[E](campos: CampoEntrada[E, _]*): E => Map[String, _] =
  input => campos.map(campo => (campo.nombre, campo.extraer(input))).toMap

def renombrando(nombres: (String, String)*): Map[String, _] => Map[String, _] =
  mapa =>
    nombres
      .map { case (nombreEntrada, nombreSalida) => (nombreSalida, mapa(nombreEntrada)) }
      .toMap

class CampoSalida[F](nombre: String, val colocar: (Map[String, _], F) => Unit) extends Campo(nombre)

trait Recolector[F, S]:
  def acumular(item: F): Unit

  def completar: S
end Recolector

def recolectandoCon[F, S](nuevoRegistroSalida: () => F,
                          recolector: Recolector[F, S],
                          campos: CampoSalida[F]*)
: Iterator[Map[String, _]] => S =
  registrosEntrada =>
    registrosEntrada.foreach { registroEntrada =>
      val registroSalida = nuevoRegistroSalida()
      campos.foreach { campo =>
        campo.colocar(registroEntrada, registroSalida)
      }
      recolector.acumular(registroSalida)
    }
    recolector.completar


def comoLista[A]: Iterator[A] => List[A] = _.toList
def copiar[E, S](leer: => Iterator[E],
                 extraer: E => Map[String, _],
                 transformar: Map[String, _] => Map[String, _],
                 recolectar: Iterator[Map[String, _]] => S): S =
  recolectar(leer.map(extraer.andThen(transformar)))