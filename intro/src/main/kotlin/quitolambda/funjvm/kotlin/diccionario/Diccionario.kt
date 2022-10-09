package quitolambda.funjvm.kotlin.diccionario

import java.io.File

fun main(args: Array<String>) {
    val Espacios = "\\s+".toRegex()
    val Palabras = "\\p{IsLatin}+".toRegex()

    val Omitidas = setOf(
        "a", "como", "con", "de", "del", "el", "en", "es", "la", "las", "más", "para", "por",
        "que", "se", "un", "una", "y"
    )

    args
        .flatMap { File(it).bufferedReader().lineSequence() }
        .flatMap { it.split(Espacios) }
        .filter { Palabras.matches(it) && !Omitidas.contains(it) }
        .groupBy { it }
        .mapValues { (_, ocurrences) -> ocurrences.size }
        .toList()
        .sortedBy { -it.second }
        .map { "${it.first}=${it.second}" }
        .forEach(::println)
}