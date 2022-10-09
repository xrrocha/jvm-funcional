package scott.infra.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public record ArchivoPropiedades(File archivo) {
    public ArchivoPropiedades(String nombreArchivo) {
        this(new File(nombreArchivo));
    }

    public File conSufijo(String sufijo) {
        return new File(
                archivo.getAbsoluteFile().getParentFile(),
                nombreBase(archivo.getName()) + "." + sufijo
        );
    }

    public Properties leerPropiedades() {
        final var properties = new Properties();
        try {
            properties.load(new FileReader(archivo));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private static String nombreBase(String nombreArchivo) {
        final var pos = nombreArchivo.lastIndexOf('.');
        if (pos < 0) return nombreArchivo;
        else return nombreArchivo.substring(0, pos);
    }
}
