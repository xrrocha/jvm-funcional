package memimg

import java.math.BigDecimal

typealias Monto = BigDecimal

data class Banco(val cuentas: MutableMap<String, Cuenta> = HashMap())

// @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS)
data class Cuenta(val id: String, val nombre: String) {
    var balance: Monto by DelegadoTransaccional(Monto.ZERO) { it >= Monto.ZERO }
}

/* 2) Application mutations: Deposit, Withdrawal, Transfer */
interface MutacionBancaria<R> : Mutacion<Banco, R> {
    fun ejecutarSobreBanco(banco: Banco): R?
    override fun ejecutarSobre(sistema: Banco): R? = ejecutarSobreBanco(sistema)
}


interface ConsultaBancaria<R> : Consulta<Banco, R> {
    fun consultarSobreBanco(banco: Banco): R?
    override fun consultarSobre(sistema: Banco): R? = consultarSobreBanco(sistema)
}

interface MutacionCuenta : MutacionBancaria<Unit> {
    val idCuenta: String
    fun aplicarA(cuenta: Cuenta)
    override fun ejecutarSobreBanco(banco: Banco) {
        aplicarA(banco.cuentas[idCuenta]!!)
    }
}

data class CrearCuenta(val id: String, val nombre: String) : MutacionBancaria<Unit> {
    override fun ejecutarSobreBanco(banco: Banco) {
        banco.cuentas[id] = Cuenta(id, nombre)
    }
}

data class Deposito(override val idCuenta: String, val monto: Monto) : MutacionCuenta {
    override fun aplicarA(cuenta: Cuenta) {
        cuenta.balance += monto
    }
}

data class Retiro(override val idCuenta: String, val monto: Monto) : MutacionCuenta {
    override fun aplicarA(cuenta: Cuenta) {
        cuenta.balance -= monto
    }
}

data class Transferencia(val idCuentaDesde: String, val idCuentaHacia: String, val monto: Monto) :
    MutacionBancaria<Unit> {
    override fun ejecutarSobreBanco(banco: Banco) {
        Deposito(idCuentaHacia, monto).ejecutarSobre(banco)
        Retiro(idCuentaDesde, monto).ejecutarSobre(banco)
    }
}
