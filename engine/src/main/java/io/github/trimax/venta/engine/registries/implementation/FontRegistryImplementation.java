package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.entity.FontEntity;
import io.github.trimax.venta.engine.model.entity.implementation.FontEntityImplementation;
import io.github.trimax.venta.engine.registries.FontRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryUtil;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_COUNT;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontRegistryImplementation
        extends AbstractRegistryImplementation<FontEntityImplementation, FontEntity, Void>
        implements FontRegistry {
    private final AtlasRegistryImplementation atlasRepository;
    private final ResourceService resourceService;

    @Override
    protected FontEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        final var buffer = resourceService.getAsBuffer(String.format("/fonts/%s.ttf", resourcePath));

        final var font = new FontEntityImplementation(buffer);
        for (int i = 0; i < FONT_ATLAS_COUNT; i++)
            font.add(atlasRepository.create(String.format("%s-%d", resourcePath, i), i, buffer));

        return font;
    }

    @Override
    protected void unload(@NonNull final FontEntityImplementation entity) {
        log.info("Unloading font {}", entity.getID());
        MemoryUtil.memFree(entity.getBuffer());
    }
}