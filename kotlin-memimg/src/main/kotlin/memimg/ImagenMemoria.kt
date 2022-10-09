package memimg

import arrow.core.Either
import arrow.core.flatMap

interface Mutacion<S, R> {
    fun ejecutarSobre(sistema: S): R?
}

interface Consulta<S, R> {
    fun consultarSobre(sistema: S): R?
}

class ImagenMemoria(private val sistema: Any, private val almacenMutaciones: AlmacenMutaciones) {

    init {
        synchronized(sistema) {
            almacenMutaciones.reEjecutar { mutacion: Mutacion<Any, Any> -> mutacion.ejecutarSobre(sistema) }
        }
    }

    fun <S, R> ejecutarMutation(mutacion: Mutacion<S, R>): Either<Falla, R?> =
        ManejadorTransacciones.correrEnTransaccion {
            @Suppress("UNCHECKED_CAST")
            Either.catch { mutacion.ejecutarSobre(sistema as S) }
                .mapLeft { FallaAplicacion("Ejecutando mutación ${mutacion::class.qualifiedName}", it) }
                .flatMap { result ->
                    Either.catch { almacenMutaciones.adicionar(mutacion) }
                        .mapLeft { FallaSistema("Serializando mutación ${mutacion::class.qualifiedName}", it) }
                        .map { result }
                }
        }

    fun <S, R> ejecutarConsulta(consulta: Consulta<S, R>): Either<Falla, R?> =
        @Suppress("UNCHECKED_CAST")
        Either.catch { consulta.consultarSobre(sistema as S) }
            .mapLeft { FallaAplicacion("Ejecutando consulta ${consulta::class.qualifiedName}", it) }
}
