package scott.infra.jpa.entidad;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(EscuchaEntidad.class)
public abstract class Entidad extends Validable {

    @Id
    @Getter
    // @GeneratedValue(generator = "UUID")
    // @GenericGenerator(
    //         name = "UUID",
    //         strategy = "org.hibernate.id.UUIDGenerator",
    //         parameters = {
    //                 @Parameter(
    //                         name = "uuid_gen_strategy_class",
    //                         value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
    //                 )
    //         }
    // )
    @Column(name = "id", nullable = false, length = 32, updatable = false)
    String id;

    @Basic
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    LocalDateTime fechaCreacion;

    @Basic
    @Column(name = "fecha_actualizacion", updatable = false)
    LocalDateTime fechaActualizacion;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entidad entidad)) {
            return false;
        }
        if (!(getClass().isAssignableFrom(o.getClass()) ||
                o.getClass().isAssignableFrom(getClass()))) {
            return false;
        }
        return id.equals(entidad.id);
    }
}

