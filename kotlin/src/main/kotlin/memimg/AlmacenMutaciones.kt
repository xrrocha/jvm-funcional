package memimg

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

interface AlmacenMutaciones {
    fun <E> reEjecutar(consumidorMutaciones: (E) -> Unit)
    fun adicionar(mutacion: Any)
}

interface Convertidor<T> {
    fun leer(valor: String): T
    fun formatear(valor: T): String
}

open class AlmacenMutacionesArchivoTexto<E : Any, C : Convertidor<E>>(
    private val archivo: File, private val
    convertidor: C
) :
    AlmacenMutaciones, AutoCloseable {

    private lateinit var escritor: PrintWriter

    init {
        require(
            (archivo.exists() && archivo.canRead() && archivo.canWrite()) ||
                    (!archivo.exists() &&
                            (archivo.parentFile.canWrite() || archivo.parentFile.mkdirs()))
        ) {
            "Archivo inaccesible ${archivo.absolutePath}"
        }
        archivo.createNewFile()
    }

    override fun <E> reEjecutar(consumidorMutaciones: (E) -> Unit) =
        archivo.bufferedReader().use { reader ->
            @Suppress("UNCHECKED_CAST")
            reader.lineSequence().map(convertidor::leer).forEach { consumidorMutaciones(it as E) }
            escritor = PrintWriter(FileWriter(archivo, true), true)
        }

    @Suppress("UNCHECKED_CAST")
    override fun adicionar(mutacion: Any) = escritor.println(convertidor.formatear(mutacion as E))

    override fun close() = escritor.close()
}
