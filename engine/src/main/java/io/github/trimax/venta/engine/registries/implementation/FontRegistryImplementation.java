package io.github.trimax.venta.engine.registries.implementation;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsFont;
import io.github.trimax.venta.engine.model.entity.FontEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.FontEntityImplementation;
import io.github.trimax.venta.engine.registries.FontRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontRegistryImplementation
        extends AbstractRegistryImplementation<FontEntityImplementation, FontEntity, Void>
        implements FontRegistry {
    private final AtlasRegistryImplementation atlasRepository;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected FontEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        final var buffer = toBuffer(resourceService.getAsBytes(String.format("/fonts/%s.ttf", resourcePath)));

        final var font = abettor.createFont(buffer);
        for (int i = 0; i < DefinitionsFont.ATLAS_COUNT; i++)
            font.add(atlasRepository.create(String.format("%s-%d", resourcePath, i), i, buffer));

        return font;
    }

    private ByteBuffer toBuffer(final byte[] bytes) {
        final var buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        return buffer;
    }

    @Override
    protected void unload(@NonNull final FontEntityImplementation entity) {
        log.info("Unloading font {}", entity.getID());
        MemoryUtil.memFree(entity.getBuffer());
    }
}