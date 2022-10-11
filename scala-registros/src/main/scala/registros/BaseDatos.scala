package registros

import java.sql.{Connection, ResultSet}
import scala.util.{Try, Using}

class CampoEntradaBD(nombre: String)
  extends CampoEntrada[ResultSet, Any](nombre, _.getObject(nombre))

def campoEntradaDB(nombre: String): CampoEntradaBD = CampoEntradaBD(nombre)

def leyendoSql(conexion: => Connection, params: Map[String, _], consulta: String): Iterator[ResultSet] =
  val consultaSql = ConsultaSql(consulta)
  val sentencia = conexion.prepareStatement(consultaSql.sql)
  for i <- consultaSql.parameterNames.indices do
    sentencia.setObject(i + 1, params(consultaSql.parameterNames(i)))
  rs2Iterator(sentencia.executeQuery())
def extrayendoSql(campos: CampoEntradaBD*): ResultSet => Map[String, _] =
  resultSet =>
    campos.map(campo => (campo.nombre, CampoEntradaBD(campo.nombre).extraer(resultSet))).toMap

case class ConsultaSql(val sql: String, val parameterNames: IndexedSeq[String])

object ConsultaSql {
  private val ParameterRef = ":[_\\p{IsLatin}][_\\p{IsLatin}\\d]+".r

  def apply(consulta: String): ConsultaSql = new ConsultaSql(
    ParameterRef.replaceAllIn(consulta, "?"),
    ParameterRef
      .findAllIn(consulta)
      .map(_.substring(1))
      .toIndexedSeq
  )
}

def rs2Iterator(rs: ResultSet): Iterator[ResultSet] =
  new Iterator[ResultSet] {
    override def hasNext: Boolean = rs.next()

    override def next(): ResultSet = rs
  }

def rs2Map(rs: ResultSet): Map[String, _] =
  (for i <- 1 to rs.getMetaData.getColumnCount
    yield (rs.getMetaData.getColumnLabel(i), rs.getObject(i)))
    .toMap

class CampoSalidaDB[E](nombre: String)
  extends CampoSalida[collection.mutable.Map[String, Any]](
    nombre,
    (registroEntrada, params) => params(nombre) = registroEntrada(nombre)
  )

case class Tabla(nombre: String, nombresColumna: String*):
  def sqlInsercion: String =
    s"""
       |INSERT INTO $nombre(${nombresColumna.mkString(", ")})
       |VALUES(${List.fill(nombresColumna.size)("?").mkString(", ")})
       |""".stripMargin

def insertandoTabla(tabla: Tabla, medidaLote: Int)
                   (conexion: => Connection, params: Map[String, _]): Iterator[Map[String, _]] => Int =
  filas =>
    val sentencia = conexion.prepareStatement(tabla.sqlInsercion)
    val cuentaFilas = filas.foldLeft(1) { case (ordinal, fila) =>
      sentencia.clearParameters()
      val bindings = params ++ fila
      for i <- tabla.nombresColumna.indices do
        sentencia.setObject(i + 1, bindings(tabla.nombresColumna(i)))
      sentencia.addBatch()
      if ordinal % medidaLote == 0 then
        sentencia.executeBatch()
      ordinal + 1
    } - 1
    if cuentaFilas % medidaLote != 0 then
      sentencia.executeBatch()
    cuentaFilas


