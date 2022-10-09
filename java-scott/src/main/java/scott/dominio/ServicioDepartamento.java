package scott.dominio;

import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scott.infra.Falla;

import static scott.infra.jpa.RepositorioDSL.*;

public interface ServicioDepartamento {
    Either<Falla, String> crearDepartamento(String codigo, String nombre, String localidad);

    Either<Falla, String> relocalizar(String idDepartamento, String nuevaLocalidad);

    @Service
    class Impl implements ServicioDepartamento {
        @Override
        public Either<Falla, String> crearDepartamento(String codigo, String nombre, String localidad) {
            return persistirInstancia(
                    repositorioDepartamento, Departamento::getId,
                    detectarDuplicado(repositorioDepartamento::buscarPorCodigo, codigo),
                    () -> Departamento.builder()
                            .codigo(codigo)
                            .nombre(nombre)
                            .localidad(localidad)
                            .build()
            );
        }

        @Override
        public Either<Falla, String> relocalizar(String idDepartamento, String nuevaLocalidad) {
            return actualizarConResultado(
                    idDepartamento, repositorioDepartamento,
                    departamento -> departamento.relocalizar(nuevaLocalidad));
        }

        private final RepositorioDepartamento repositorioDepartamento;

        @Autowired
        public Impl(RepositorioDepartamento repositorioDepartamento) {
            this.repositorioDepartamento = repositorioDepartamento;
        }
    }
}

