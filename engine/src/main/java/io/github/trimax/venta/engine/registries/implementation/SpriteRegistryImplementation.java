package io.github.trimax.venta.engine.registries.implementation;

import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.dto.SpriteDTO;
import io.github.trimax.venta.engine.model.dto.common.FrameDTO;
import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.registries.SpriteRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpriteRegistryImplementation
        extends AbstractRegistryImplementation<SpriteEntityImplementation, SpriteEntity, Void>
        implements SpriteRegistry {
    private final TextureRegistryImplementation textureRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;

    @Override
    protected SpriteEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        log.info("Loading sprite {}", resourcePath);

        final var spriteDTO = resourceService.getAsObject(String.format("/sprites/%s", resourcePath), SpriteDTO.class);
        final var texture = textureRegistry.get(spriteDTO.texture());

        final var framesBuffer = MemoryUtil.memAllocFloat(spriteDTO.frames().size() * 4);
        for (final FrameDTO frame : spriteDTO.frames()) {
            final var normalizedFrame = frame.normalize(texture.getWidth(), texture.getHeight());
            framesBuffer.put(normalizedFrame.x());
            framesBuffer.put(normalizedFrame.y());
            framesBuffer.put(normalizedFrame.width());
            framesBuffer.put(normalizedFrame.height());
        }

        framesBuffer.flip();

        return abettor.createSprite(texture, framesBuffer,
                spriteDTO.color().toVector4f(),
                spriteDTO.looping(),
                spriteDTO.frames().size(),
                spriteDTO.duration());
    }


    @Override
    protected void unload(@NonNull final SpriteEntityImplementation entity) {
        log.info("Unloading sprite {}", entity.getID());
        MemoryUtil.memFree(entity.getFramesBuffer());
    }
}
