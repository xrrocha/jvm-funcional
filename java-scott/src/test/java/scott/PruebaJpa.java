package scott;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
public abstract class PruebaJpa extends PruebaIntegracion {

    @Autowired
    protected TestEntityManager entityManager;

    @BeforeEach
    public void iniciarTransaccion() {
        if (!entityManager.getEntityManager().getTransaction().isActive()) {
            entityManager.getEntityManager().getTransaction().begin();
        }
    }

    @AfterEach
    public void terminarTransaccion() {
        entityManager.getEntityManager().getTransaction().rollback();
    }

    protected <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    protected <T> List<T> toList(Iterable<T> iterable) {
        List<T> lista = new ArrayList<>();
        iterable.forEach(lista::add);
        return lista;
    }
}
