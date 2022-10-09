package memimg

import arrow.core.Either
import arrow.core.left
import arrow.core.right

interface Validador<out T> {
    fun validar(valor: @UnsafeVariance T?): Either<String, Unit>
}

open class ValidadorRegex(private val regex: Regex, private val mensaje: String) : Validador<String> {
    constructor(pattern: String, message: String) : this(pattern.toRegex(), message)

    override fun validar(valor: String?): Either<String, Unit> =
        when {
            valor == null || regex.matchEntire(valor) != null -> Unit.right()
            else -> "$mensaje: $valor".left()
        }
}
