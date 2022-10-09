package scott;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class PruebaIntegracion {

    @Autowired
    protected ApplicationContext applicationContext;

    protected <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}