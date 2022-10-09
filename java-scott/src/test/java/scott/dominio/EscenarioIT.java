package scott.dominio;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import scott.PruebaIntegracion;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static scott.dominio.Genero.FEMENINO;
import static scott.dominio.Genero.MASCULINO;
import static scott.infra.jpa.RepositorioDSL.leer;

@Transactional
@SpringBootTest
public class EscenarioIT extends PruebaIntegracion {

    @Test
    public void dslFunciona() {

        final var contabilidad = crearDepartamento("10", "Contabilidad", "Quito");
        final var investigacion = crearDepartamento("20", "Investigación", "Sunrise");
        final var ventas = crearDepartamento("30", "Ventas", "Bogota");

        assertEquals(
                HashSet.of(contabilidad, investigacion, ventas),
                List.ofAll(repositorioDepartamento.findAll()).toSet()
        );

        final var king = crearEmpleado(
                "7839", "King", FEMENINO, "Presidente",
                null, LocalDate.of(2011, 11, 17),
                new BigDecimal(15000), null, contabilidad);

        final var jones = crearEmpleado(
                "7566", "Jones", MASCULINO, "Gerente",
                king, LocalDate.of(2011, 4, 2),
                new BigDecimal(14875), null, investigacion);

        final var blake = crearEmpleado(
                "7698", "Blake", MASCULINO, "Gerente", king,
                LocalDate.of(2011, 1, 1),
                new BigDecimal(14250), null, ventas);

        final var allen = crearEmpleado(
                "7499", "Allen", MASCULINO, "Vendedor", blake,
                LocalDate.of(2011, 2, 20),
                new BigDecimal(8000), new BigDecimal(1500), ventas);

        assertEquals(
                HashSet.of(king, jones, blake, allen),
                List.ofAll(repositorioEmpleado.findAll()).toSet()
        );

        assertEquals("Vendedor", allen.getCargo());
        assertEquals(blake, allen.getSupervisor());
        assertEquals(new BigDecimal(8000), allen.getSalario());
        assertEquals(new BigDecimal(1500), allen.getComision());
        assertEquals(ventas, allen.getDepartamento());

        servicioEmpleado.reasignar(
                allen.getId(), contabilidad.getId(), "Oficinista", king.getId(),
                new BigDecimal(5000), null);

        final var allenDespues = leer(repositorioEmpleado, allen.getId());
        assertEquals("Oficinista", allenDespues.getCargo());
        assertEquals(king, allenDespues.getSupervisor());
        assertEquals(new BigDecimal(5000), allenDespues.getSalario());
        assertNull(allenDespues.getComision());
        assertEquals(contabilidad, allen.getDepartamento());
    }

    private Departamento crearDepartamento(String codigo, String nombre, String localidad) {
        return servicioDepartamento.crearDepartamento(codigo, nombre, localidad)
                .map(idDepartamento -> leer(repositorioDepartamento, idDepartamento))
                .get();
    }

    private Empleado crearEmpleado(String codigo,
                                   String nombre,
                                   Genero genero,
                                   String cargo,
                                   Empleado supervisor,
                                   LocalDate fechaContratacion,
                                   BigDecimal salario,
                                   BigDecimal comision,
                                   Departamento departamento) {
        return servicioEmpleado.crearEmpleado(
                        codigo, nombre, genero, cargo,
                        supervisor == null ? null : supervisor.getId(),
                        fechaContratacion, salario, comision, departamento.getId())
                .map(idEmpleado -> leer(repositorioEmpleado, idEmpleado))
                .get();
    }

    private final RepositorioDepartamento repositorioDepartamento;
    private final RepositorioEmpleado repositorioEmpleado;
    private final ServicioDepartamento servicioDepartamento;
    private final ServicioEmpleado servicioEmpleado;

    @Autowired
    public EscenarioIT(RepositorioDepartamento repositorioDepartamento,
                       RepositorioEmpleado repositorioEmpleado,
                       ServicioDepartamento servicioDepartamento,
                       ServicioEmpleado servicioEmpleado) {
        this.repositorioDepartamento = repositorioDepartamento;
        this.repositorioEmpleado = repositorioEmpleado;
        this.servicioDepartamento = servicioDepartamento;
        this.servicioEmpleado = servicioEmpleado;
    }
}
