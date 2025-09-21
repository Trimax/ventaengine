package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.model.common.dto.Frame;
import io.github.trimax.venta.engine.model.dto.SpriteDTO;
import io.github.trimax.venta.engine.model.entity.SpriteEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.SpriteEntityImplementation;
import io.github.trimax.venta.engine.registries.SpriteRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;

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

        return abettor.createSprite(texture,
                normalizeFrames(spriteDTO.frames(), texture.getWidth(), texture.getHeight()),
                spriteDTO.looping(),
                spriteDTO.duration());
    }

    private List<Frame> normalizeFrames(@NonNull final List<Frame> frames, final int textureWidth, final int textureHeight) {
        return StreamEx.of(frames)
                .map(frame -> frame.normalize(textureWidth, textureHeight))
                .toList();
    }

    @Override
    protected void unload(@NonNull final SpriteEntityImplementation entity) {
        log.info("Unloading sprite {}", entity.getID());
    }
}
