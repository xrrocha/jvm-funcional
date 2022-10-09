package scott.dominio;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import scott.dominio.Genero.ConvertidorGenero;
import scott.infra.jpa.entidad.Entidad;
import scott.infra.validacion.ValorInvalido;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleado", uniqueConstraints = {
        @UniqueConstraint(name = "empl_uk_codigo", columnNames = {"codigo"})})
@Getter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Empleado extends Entidad {
    @ToString.Include
    @Pattern(regexp = "^[0-9]{4}$", message = "Código de empleado inválido; debe constar de cuatro dígitos")
    @NotNull(message = "El código del empleado debe ser especificado")
    @Basic(optional = false)
    @Column(name = "codigo", nullable = false, length = 4)
    private String codigo;

    @ToString.Include
    @Pattern(regexp = "^\\p{IsLatin}{2,16}$", message = "Nombre de empleado inválido; solo puede contener letras")
    @NotNull(message = "El nombre del empleado debe ser especificado")
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 16)
    private String nombre;

    @ToString.Include
    @Basic(optional = false)
    @Column(name = "genero", length = 1)
    @Convert(converter = ConvertidorGenero.class)
    private Genero genero;

    @ToString.Include
    @Pattern(regexp = "^\\p{IsLatin}{2,16}$", message = "Cargo de empleado inválido; solo puede contener letras")
    @NotNull(message = "El cargo del empleado debe ser especificado")
    @Basic(optional = false)
    @Column(name = "cargo", nullable = false, length = 16)
    private String cargo;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_supervisor",
            foreignKey = @ForeignKey(name = "empl_fk_supervisor"))
    private Empleado supervisor;

    @Basic
    @NotNull(message = "La fecha de contratación del empleado debe ser especificada")
    @Column(name = "fecha_contratacion", nullable = false, updatable = false)
    LocalDate fechaContratacion;

    @NotNull(message = "El salario del empleado debe ser provisto")
    @Positive(message = "El salario del empleado debe ser positivo")
    @Basic(optional = false)
    @Column(name = "salario", nullable = false)
    private BigDecimal salario;

    @Positive(message = "La comisión del empleado debe ser positiva")
    @Basic
    @Column(name = "comision")
    private BigDecimal comision;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_departamento",
            foreignKey = @ForeignKey(name = "empl_fk_departamento"))
    private Departamento departamento;

    @Builder
    public Empleado(String codigo,
                    String nombre,
                    Genero genero,
                    String cargo,
                    Empleado supervisor,
                    LocalDate fechaContratacion,
                    BigDecimal salario,
                    BigDecimal comision,
                    Departamento departamento) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.genero = genero;
        this.cargo = cargo;
        this.supervisor = supervisor;
        this.fechaContratacion = fechaContratacion;
        this.salario = salario;
        this.comision = comision;
        this.departamento = departamento;
        validar();
    }

    public void reasignar(Departamento departamento,
                          String cargo,
                          Empleado supervisor,
                          BigDecimal salario,
                          BigDecimal comision) {
        this.departamento = departamento;
        this.cargo = cargo;
        this.supervisor = supervisor;
        this.salario = salario;
        this.comision = comision;
        validar();
    }

    private void validar() {
        validarInstancia(vis -> {
            if (!"Ventas".equalsIgnoreCase(departamento.getNombre()) && comision != null) {
                vis.add(new ValorInvalido(
                        "comision", comision,
                        "La comisión (%s) solo aplica al departamento VENTAS, no al departamento %s"
                                .formatted(comision, departamento.getNombre())));
            }
        });
    }
}
