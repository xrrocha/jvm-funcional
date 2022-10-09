package memimg

class Probador<S>(private val imagenMemoria: ImagenMemoria) {
    fun <R> verificarEfecto(mutacion: Mutacion<S, R>, aserto: (R?) -> Boolean) =
        imagenMemoria.ejecutarMutation(mutacion)
            .tapLeft(Falla::tirarExcepcion)
            .tap { result ->
                if (aserto(result).not())
                    throw IllegalArgumentException("Assertion failed for $mutacion")
            }

    fun <R> verificarQue(mutacion: Mutacion<S, R>, aserto: (R?) -> Unit) =
        imagenMemoria.ejecutarMutation(mutacion)
            .tapLeft(Falla::tirarExcepcion)
            .tap(aserto)
}