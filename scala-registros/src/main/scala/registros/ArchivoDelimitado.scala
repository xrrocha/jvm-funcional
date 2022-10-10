package registros

import scala.util.matching.Regex

def separarPorDelimitador(cadena: String, delimitador: Regex): IndexedSeq[String] =
  delimitador.split(cadena).toIndexedSeq

def separarPorDelimitador(cadena: String, delimitador: String): IndexedSeq[String] =
  separarPorDelimitador(cadena, delimitador.r)

def dalimitadorEntrada(delimitador: String): String => IndexedSeq[String] =
  separarPorDelimitador(_, delimitador)

class CampoDelimitado[S](nombre: String,
                         posicion: Int,
                         extraer: String => S)
  extends CampoEntrada[IndexedSeq[String], S](nombre, is => extraer(is(posicion)))

def campoDelimitado(nombre: String, posicion: Int): CampoDelimitado[String] =
  CampoDelimitado(nombre, posicion, identity)

def campoDelimitado[S](nombre: String,
                       posicion: Int,
                       extraer: String => S): CampoDelimitado[S] =
  CampoDelimitado(nombre, posicion, extraer)
