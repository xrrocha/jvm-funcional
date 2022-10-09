package scott.dominio;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class ServicioDepartamentoComplicado {
    public String crearDepartamento(String codigo, String nombre, String localidad) {
        // Valida que el código de departamento no sea duplicado
        final Optional<Departamento> optDepartamento;
        try {
            optDepartamento = repositorioDepartamento.buscarPorCodigo(codigo);
        } catch (Exception e) {
            throw new RuntimeException("Error recuperando departamento por código", e);
        }
        optDepartamento.ifPresent(d -> {
            String mensaje = "Ya existe un departamento con codigo %s: %s!".formatted(codigo, d.getNombre());
            throw new IllegalArgumentException(mensaje);
        });

        // Construye instancia de departamento
        final Departamento departamento;
        try {
            departamento = Departamento.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .localidad(localidad)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error de validación creando departamento", e);
        }

        // Persiste nuevo departamento
        final Departamento departamentoGuardado;
        try {
            departamentoGuardado = repositorioDepartamento.guardar(departamento);
        } catch (Exception e) {
            throw new RuntimeException("Error guardando nuevo departamento", e);
        }

        // Retorna id generado para nuevo departamento
        return departamentoGuardado.getId();
    }

    private final RepositorioDepartamento repositorioDepartamento;

    @Autowired
    public ServicioDepartamentoComplicado(RepositorioDepartamento repositorioDepartamento) {
        this.repositorioDepartamento = repositorioDepartamento;
    }
}
