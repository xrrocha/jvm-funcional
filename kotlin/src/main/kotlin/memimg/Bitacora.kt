package memimg

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.logging.LogManager

fun inicializarBitacora() {
    inicializarBitacora("logging.properties")
}

fun inicializarBitacora(nombreRecurso: String) {
    openResource(nombreRecurso)
        .map(LogManager.getLogManager()::readConfiguration)
        .getOrElse { throw IllegalStateException("No se puede localizar recurso $nombreRecurso") }
}

fun openResource(nombreRecurso: String): Either<FileNotFoundException, InputStream> =
    ImagenMemoria::class.java.classLoader.getResourceAsStream(nombreRecurso)?.right()
        ?: FileNotFoundException("No existe recurso: $nombreRecurso").left()
