package memimg

import java.util.logging.Level
import java.util.logging.Logger

sealed class Falla(level: String, context: String, cause: Throwable? = null) {

    val message = "Error de $level: $context${cause?.let { " (${cause.message ?: cause})" } ?: ""}"
    private val exception by lazy { RuntimeException(message, cause) }

    fun comoExcepcion() = exception

    fun tirarExcepcion(): Nothing = throw exception

    override fun toString(): String = "Falla($message)"
}

class FallaSistema(context: String, cause: Throwable) : Falla("sistema", context, cause)

open class FallaAplicacion(context: String, cause: Throwable? = null) : Falla("aplicación", context, cause)

fun Logger.log(falla: Falla, logStackTrace: Boolean = false) {
    val loggingLevel = when (falla) {
        is FallaSistema -> Level.SEVERE
        else -> Level.WARNING
    }
    if (logStackTrace) log(loggingLevel, falla.message, falla)
    else log(loggingLevel, falla.message)
}