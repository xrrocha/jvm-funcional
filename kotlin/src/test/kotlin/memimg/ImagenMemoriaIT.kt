package memimg

import arrow.core.getOrElse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImagenMemoriaIT {

    init {
        inicializarBitacora()
    }

    @Test
    fun `La imagen de memoria funciona`() {

        val banco = Banco()
        val almacenMutaciones = AlmacenMutacionesEnMemoria()
        val imagenMemoria = ImagenMemoria(banco, almacenMutaciones)

        fun balanceDe(id: String) = banco.cuentas[id]!!.balance.toInt()

        with(Probador<Banco>(imagenMemoria)) {

            verificarEfecto(CrearCuenta("janet", "Janet Doe")) {
                balanceDe("janet") == 0
            }

            verificarEfecto(Deposito("janet", Monto(100))) {
                balanceDe("janet") == 100
            }

            verificarEfecto(Retiro("janet", Monto(10))) {
                balanceDe("janet") == 90
            }

            verificarEfecto(CrearCuenta("john", "John Doe")) {
                balanceDe("john") == 0
            }

            verificarEfecto(Deposito("john", Monto(50))) {
                balanceDe("john") == 50
            }

            verificarQue(Transferencia("janet", "john", Monto(20))) {
                assertEquals(70, balanceDe("janet"))
                assertEquals(70, balanceDe("john"))
            }
        }

        data class ConsultaPorId(val id: String) : ConsultaBancaria<Cuenta> {
            override fun consultarSobreBanco(banco: Banco): Cuenta? =
                banco.cuentas.values.find { it.id == id }
        }

        val query = ConsultaPorId("janet")
        val queryResult = imagenMemoria.ejecutarConsulta(query)
        assertTrue(queryResult.isRight())
        val queryValue = queryResult.getOrElse { null }
        assertTrue(
            queryValue != null &&
                    queryValue.id == "janet" &&
                    queryValue.balance == Monto(70)
        )
    }
}
