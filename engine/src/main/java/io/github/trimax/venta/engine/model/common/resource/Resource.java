package io.github.trimax.venta.engine.model.common.resource;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.enums.ResourceType;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public record Resource(@NonNull ResourceType type, @NonNull String name, String reference) {
    public static Resource asGroup(final GroupType type) {
        return new Resource(ResourceType.Group, type.getDisplayName(), type.name());
    }

    public static Resource asFolder(final String name) {
        return new Resource(ResourceType.Folder, name, null);
    }

    public static Resource asResource(final String name, final String reference) {
        return new Resource(ResourceType.File,  name, reference);
    }

    public boolean hasExistingReference() {
        return StringUtils.isNotBlank(reference) && Files.exists(Path.of(reference));
    }
}
