package io.github.trimax.venta.core.model.archive;

public record ArchiveResource(String name, int size, Class<?> type, byte[] data) {
}
