package scott.infra.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public record Argumentos(Map<String, List<String>> args) {

    public Argumentos(String[] args) {
        this(Arrays.stream(args)
                .filter(arg -> arg.startsWith("--") && arg.contains("="))
                .map(arg -> {
                    final var pos = arg.indexOf('=');
                    final var argName = arg.substring(2, pos);
                    final var argValue = arg.substring(pos + 1);
                    return new SimpleEntry<>(argName, argValue);
                })
                .collect(Collectors.groupingBy(Entry::getKey))
                .entrySet().stream()
                .map(entry -> new SimpleEntry<>(
                        entry.getKey(),
                        entry.getValue().stream().map(SimpleEntry::getValue).toList()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

    public List<String> leerArgumentos(String argName) {
        return args.getOrDefault(argName, Collections.emptyList());
    }

    public String leerArgumento(String argName, String valorPorDefecto) {
        return args.getOrDefault(argName, List.of(valorPorDefecto)).get(0);
    }

    @Override
    public String toString() {
        return args.toString();
    }
}
