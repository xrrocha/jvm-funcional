package scott.infra.jpa;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import scott.infra.util.ArchivoPropiedades;
import scott.infra.util.Argumentos;

import javax.persistence.Entity;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class GeneradorEsquemas {

    private static final Logger logger = LoggerFactory.getLogger(GeneradorEsquemas.class);

    public static void main(String[] args) {

        final var argumentos = new Argumentos(args);
        final var nombrePaquete = argumentos.leerArgumento("paquete-java", "");
        final var archivosPropiedades = argumentos.leerArgumentos("propiedades-db");

        logger.info("Generando esquema(s) para paquete '{}': {}", nombrePaquete, archivosPropiedades);

        archivosPropiedades.stream()
                .map(ArchivoPropiedades::new)
                .forEach(archivoPropiedades -> generarEsquema(archivoPropiedades, nombrePaquete));
    }

    private static void generarEsquema(ArchivoPropiedades archivoPropiedades, String nombrePaquete) {
        logger.info("Generando esquema para {}", archivoPropiedades.archivo().getName());

        final var metadataSources =
                new MetadataSources(new StandardServiceRegistryBuilder()
                        .loadProperties(archivoPropiedades.archivo())
                        .build());
        cargarEntidades(nombrePaquete).forEach(metadataSources::addAnnotatedClass);
        Metadata metadata = metadataSources.buildMetadata();

        var archivoEsquema = archivoPropiedades.conSufijo("sql");
        archivoEsquema.delete();

        new SchemaExport()
                .setFormat(true)
                .setManageNamespaces(true)
                .setDelimiter(";")
                .setOutputFile(archivoEsquema.getAbsolutePath())
                .createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
    }

    private static List<Class<?>> cargarEntidades(String nombrePaquete) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        return scanner.findCandidateComponents(nombrePaquete).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(GeneradorEsquemas::classForName)
                .collect(Collectors.toList());
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

