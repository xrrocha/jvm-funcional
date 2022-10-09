package memimg

class AlmacenMutacionesEnMemoria : AlmacenMutaciones {
    private val buffer = mutableListOf<Any>()
    override fun <E> reEjecutar(consumidorMutaciones: (E) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        buffer.forEach { consumidorMutaciones(it as E) }
    }

    override fun adicionar(mutacion: Any) {
        buffer += mutacion
    }
}