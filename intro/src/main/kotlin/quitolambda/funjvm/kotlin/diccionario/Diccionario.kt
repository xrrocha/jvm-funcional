package quitolambda.funjvm.kotlin.diccionario

import java.io.File

fun main(args: Array<String>) {
    val espacios = "\\s+".toRegex()
    val palabras = "\\p{IsLatin}+".toRegex()
    val omitidas = setOf("a", "como", "con", "de", "del", "el", "en", "es", "la", "las", "más", "para", "por",
                         "que", "se", "un", "una", "y")
    args
        .flatMap { File(it).bufferedReader().lineSequence() }
        .flatMap { it.split(espacios) }
        .filter { palabras.matches(it) && !omitidas.contains(it) }
        .groupBy { it }
        .mapValues { (_, ocurrences) -> ocurrences.size }
        .toList()
        .sortedBy { -it.second }
        .map { "${it.first}=${it.second}" }
        .forEach(::println)
}