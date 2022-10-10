package registros

import java.sql.{Connection, ResultSet}
import scala.util.{Try, Using}

def leyendoSql() = ???

def mapaRs(rs: ResultSet): Map[String, _] = {
  for i <- 1 to rs.getMetaData.getColumnCount
    yield (rs.getMetaData.getColumnLabel(i), rs.getObject(i))
}
  .toMap

def lectorRs(rs: ResultSet): Iterator[Map[String, _]] = new Iterator[Map[String, _]] {
  override def hasNext: Boolean = rs.next()

  override def next(): Map[String, _] = mapaRs(rs)
}

case class Tabla(nombre: String, nombresColumna: List[String]):
  def sqlInsercion: String =
    s"""
       |INSERT INTO $nombre(${nombresColumna.mkString(", ")})
       |VALUES(${List.fill(nombresColumna.size)("?").mkString(", ")})
       |""".stripMargin

def insertandoTabla(tabla: Tabla, medidaLote: Int)
                   (fuenteDatos: => Connection, params: Map[String, _]) : Iterator[Map[String, _]] => Try[Int] =
  filas => Using(fuenteDatos) { conexion =>
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

