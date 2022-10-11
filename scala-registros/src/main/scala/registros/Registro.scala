package registros

import java.io.*
import java.sql.{Connection, ResultSet}
import java.text.DecimalFormat
import scala.util.matching.Regex
import scala.util.{Try, Using}

trait Campo(val nombre: String)

def formatoNumerico(patron: String, multiplicador: Int = 1): Number => String =
  val formato = DecimalFormat(patron)
  (valor: Number) => formato.format(BigDecimal(valor.toString) * multiplicador)

def convertidorNumerico(patron: String, divisor: Int = 1): String => Number =
  val formato = DecimalFormat(patron)
  valor => BigDecimal(formato.parse(valor).toString) / divisor

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