package scott.infra.jpa.entidad;

import scott.infra.util.Identificadores;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

public class EscuchaEntidad {
    @PrePersist
    public void antesDePesistir(Entidad entidad) {
        prePersist(entidad);
    }

    @PreUpdate
    public void despuesDeActualizar(Entidad entidad) {
        preUpdate(entidad);
    }

    public static void prePersist(Entidad entidad) {
        if (entidad.id == null) {
            entidad.id = Identificadores.siguienteIdentificador();
            entidad.fechaCreacion = LocalDateTime.now();
        }
    }

    public static void preUpdate(Entidad entidad) {
        entidad.fechaActualizacion = LocalDateTime.now();
    }
}
