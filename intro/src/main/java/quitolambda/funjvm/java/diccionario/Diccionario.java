package quitolambda.funjvm.java.diccionario;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Diccionario {
    public static void main(String[] args) {
        final var espacios = Pattern.compile("\\s+");
        final var palabras = Pattern.compile("\\p{IsLatin}+");
        final var omitidas = Set.of("a", "como", "con", "de", "del", "el", "en", "es", "la", "las", "más", "para", "por", "que", "se", "un", "una", "y");
        Arrays.stream(args)
                .flatMap(::leerArchivo)
                .flatMap(linea -> Arrays.stream(espacios.split(linea)))
                .filter(palabra -> !omitidas.contains(palabra))
                .map(palabras::matcher)
                .filter(Matcher::matches)
                .map(Matcher::group)
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> (int) (e2.getValue() - e1.getValue()))
                .forEach(System.out::println);
    }
    Stream<String> leerArchivo(String nombreArchivo) {
        try {
            return new BufferedReader(new FileReader(nombreArchivo)).lines();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
