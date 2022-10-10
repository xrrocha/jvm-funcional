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

def mapaRs(rs: ResultSet): Map[String, _] = {
  for i <- 1 to rs.getMetaData.getColumnCount
    yield (rs.getMetaData.getColumnLabel(i), rs.getObject(i))
}
  .toMap

def lectorRs(rs: ResultSet): Iterator[Map[String, _]] = new Iterator[Map[String, _]] {
  override def hasNext: Boolean = rs.next()

  override def next(): Map[String, _] = mapaRs(rs)
}

def separarPorDelimitador(cadena: String, delimitador: Regex): IndexedSeq[String] =
  delimitador.split(cadena).toIndexedSeq
def separarPorDelimitador(cadena: String, delimitador: String): IndexedSeq[String] =
  separarPorDelimitador(cadena, delimitador.r)

def delimitador(delimitador: String): (String) => IndexedSeq[String] =
  separarPorDelimitador(_, delimitador)

trait Campo(val nombre: String)

class CampoEntrada[E, S](nombre: String, val extraer: E => S) extends Campo(nombre)

class CampoDelimitado[S](nombre: String,
                         posicion: Int,
                         extraer: String => S)
  extends CampoEntrada[IndexedSeq[String], S](nombre, is => extraer(is(posicion)))

def campo(nombre: String, posicion: Int): CampoDelimitado[String] =
  CampoDelimitado(nombre, posicion, identity)

def campo[S](nombre: String,
             posicion: Int,
             extraer: String => S): CampoDelimitado[S] =
  CampoDelimitado(nombre, posicion, extraer)

class CampoFijoEntrada[S](nombre: String,
                          posicion: Int,
                          longitud: Int,
                          extraer: String => S)
  extends CampoEntrada[Array[Char], S](nombre, a => extraer(String(a, posicion, longitud)))

def campos[C <: CampoEntrada[_, _]](campos: C*) = campos.toList

def extrayendoCon[I, E](preprocesar: I => E,
                        campos: CampoEntrada[E, _]*): I => Map[String, _] =
  input =>
    val valoresCampo = preprocesar(input)
    campos.map(campo => (campo.nombre, campo.extraer(valoresCampo))).toMap

def renombrando(nombres: (String, String)*): Map[String, _] => Map[String, _] =
  mapa =>
    nombres
      .map { case (nombreEntrada, nombreSalida) => (nombreSalida, mapa(nombreEntrada)) }
      .toMap

case class Tabla(nombre: String, nombresColumna: List[String]):
  def sqlInsercion: String =
    s"""
       |INSERT INTO $nombre(${nombresColumna.mkString(", ")})
       |VALUES(${List.fill(nombresColumna.size)("?").mkString(", ")})
       |""".stripMargin

def construirReducidor(fuenteConexion: => Connection,
                       tabla: Tabla,
                       medidaLote: Int,
                       params: Map[String, _]): Iterator[Map[String, _]] => Try[Int] =
  (filas: Iterator[Map[String, _]]) =>
    Using(fuenteConexion) { conexion =>
      val sentencia = conexion.prepareStatement(tabla.sqlInsercion)
      filas.foldLeft(0) { case (numeroFila, fila) =>
        sentencia.clearParameters()
        val bindings = params ++ fila
        for i <- 1 to tabla.nombresColumna.size do
          sentencia.setObject(i, bindings(tabla.nombresColumna(i)))
        sentencia.addBatch()
        if numeroFila % medidaLote == 0 then
          sentencia.execute()
        numeroFila + 1
      }
    }

def reduciendoALista[A]: Iterator[A] => List[A] = _.toList
def copiar[I, E, O](lector: => Iterator[I],
                    extraer: I => Map[String, _],
                    transformar: Map[String, _] => Map[String, _],
                    reducir: Iterator[Map[String, _]] => O): O =
  reducir(lector.map(extraer.andThen(transformar)))