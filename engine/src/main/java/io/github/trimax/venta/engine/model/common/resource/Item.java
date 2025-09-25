package io.github.trimax.venta.engine.model.common.resource;

import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.enums.ResourceType;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

//TODO: Rename to resource
public record Item(ResourceType type, String name, String reference) {
    public static Item asGroup(final GroupType type) {
        return new Item(ResourceType.Group, type.getDisplayName(), type.name());
    }

    public static Item asFolder(final String name) {
        return new Item(ResourceType.Folder, name, null);
    }

    public static Item asResource(final String name, final String reference) {
        return new Item(ResourceType.File,  name, reference);
    }

    public boolean hasExistingReference() {
        return StringUtils.isNotBlank(reference) && Files.exists(Path.of(reference));
    }
}
