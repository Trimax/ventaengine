package io.github.trimax.venta.core.model.archive;

import java.util.Map;

public record ArchiveIndex(String name, Map<String, Integer> references) {
}
