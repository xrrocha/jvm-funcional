package quitolambda.funjvm.scala.diccionario
import scala.io.Source
import scala.util.Using.resource

object Diccionario:
  val Espacios = "\\s+".r
  val Palabras = "\\p{IsLatin}+".r
  val Omitidas = Set("a", "como", "con", "de", "del", "el", "en", "es", "la", "las", "más", "para", "por",
                     "que", "se", "un", "una", "y")

  @main
  def imprimir(archivos: String*) =
    val palabras = for
      archivo <- archivos
      linea <- resource(Source.fromFile(archivo))(_.getLines().toList)
      palabra <- Espacios.split(linea)
      if Palabras.matches(palabra) && !Omitidas.contains(palabra)
    yield palabra.toLowerCase()

    palabras
      .groupBy(identity)
      .toSeq
      .map { case (palabra, ocurrencias) => (palabra, ocurrencias.size) }
      .sortBy { case (_, ocurrencias) => -ocurrencias }
      .foreach(println)
