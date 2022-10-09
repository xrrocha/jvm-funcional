package memimg

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import kotlin.reflect.KProperty

object ManejadorTransacciones {

    private val diario = ThreadLocal<MutableMap<Pair<Any, String>, () -> Unit>>().apply {
        set(mutableMapOf())
    }

    fun <R> correrEnTransaccion(accion: () -> Either<Falla, R>): Either<Falla, R> =
        synchronized(this) {
            iniciarTransaccion()
            try {
                accion()
            } catch (t: Throwable) {
                FallaSistema("Manejando transacción: $t", t).left()
            }
        }
            .tapLeft {
                deshacerTransaccion()
            }

    fun <T> recordar(who: Any, what: String, value: T, deshacer: (T) -> Unit) {
        diario.get().computeIfAbsent(Pair(who, what)) { { deshacer(value) } }
    }

    private fun iniciarTransaccion() = diario.get().clear()

    private fun deshacerTransaccion() =
        diario.get().forEach { (quienQue, deshacer) ->
            try {
                deshacer.invoke()
            } catch (t: Throwable) {
                val (quien, que) = quienQue
                throw IllegalStateException("Error retrayendo ${quien::class.simpleName}.$que: $t", t)
            }
        }
}

class DelegadoTransaccional<T>(valorInicial: T, private val validador: Validador<T>? = null) {
    private var valor: T
    private val asignador: (T) -> Unit = { value -> this.valor = value }

    constructor(valorInicial: T, validacion: (T) -> Boolean) : this(valorInicial) {
        when {
            valor == null || validacion(valor) -> Unit.right()
            else -> "Valor inválido: $valor".left()
        }
    }

    init {
        validador?.validar(valorInicial)?.getOrHandle { throw IllegalArgumentException(it) }
        valor = valorInicial
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = valor

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        validador?.validar(value)?.getOrHandle { throw IllegalArgumentException("${property.name}: $it") }
        ManejadorTransacciones.recordar(thisRef, property.name, this.valor, asignador)
        asignador(value)
    }
}
