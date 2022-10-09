package scott.dominio;

import lombok.*;
import scott.infra.jpa.entidad.Entidad;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departamento", uniqueConstraints = {
        @UniqueConstraint(name = "dept_uk_codigo", columnNames = {"codigo"})})
@Getter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Departamento extends Entidad {
    @ToString.Include
    @Pattern(regexp = "^[0-9]{2}$", message = "Código de departamento inválido; debe constar de dos dígitos")
    @NotNull(message = "El código del departamento debe ser especificado")
    @Basic(optional = false)
    @Column(name = "codigo", nullable = false, length = 2)
    private String codigo;

    @ToString.Include
    @Pattern(regexp = "^\\p{IsLatin}{2,16}$", message = "Nombre de departamento inválido; solo puede contener letras")
    @NotNull(message = "El nombre del departamento debe ser especificado")
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 16)
    private String nombre;

    @ToString.Include
    @Pattern(regexp = "^\\p{IsLatin}{2,16}$", message = "Localidad de departamento inválida; solo puede contener letras")
    @NotNull(message = "La localidad del departamento debe ser especificada")
    @Basic(optional = false)
    @Column(name = "localidad", nullable = false, length = 16)
    private String localidad;

    @OneToMany(mappedBy = "departamento", cascade = CascadeType.ALL)
    private final Set<Empleado> empleados = new HashSet<>();

    @Builder
    public Departamento(String codigo, String nombre, String localidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.localidad = localidad;
        validarAtributos();
    }

    public String relocalizar(String nuevaLocalidad) {
        String localidadOriginal = this.localidad;
        this.localidad = nuevaLocalidad;
        validarAtributos();
        return localidadOriginal;
    }
}
