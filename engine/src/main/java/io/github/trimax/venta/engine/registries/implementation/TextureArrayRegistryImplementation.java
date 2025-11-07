package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsTextureArray;
import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.dto.TextureArrayDTO;
import io.github.trimax.venta.engine.model.entity.TextureArrayEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.TextureArrayEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.TextureEntityImplementation;
import io.github.trimax.venta.engine.registries.TextureArrayRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.opengl.GL42C.glTexStorage3D;
import static org.lwjgl.opengl.GL43C.glCopyImageSubData;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureArrayRegistryImplementation
        extends AbstractRegistryImplementation<TextureArrayEntityImplementation, TextureArrayEntity, Void>
        implements TextureArrayRegistry {
    private final TextureRegistryImplementation textureRegistry;
    private final ResourceService resourceService;
    private final Abettor abettor;
    private final Memory memory;

    public TextureArrayEntityImplementation getDefaultTextureArray() {
        return get(DefinitionsTextureArray.DEFAULT);
    }

    public TextureArrayEntityImplementation create(@NonNull final String name, @NonNull final List<TextureEntityImplementation> textures) {
        return get(name, () -> {
            if (textures.isEmpty())
                throw new IllegalArgumentException("Textures array can't be empty");

            final var width = textures.getFirst().getWidth();
            final var height = textures.getFirst().getHeight();
            final var layers = textures.size();

            final var format = getTextureFormat(textures);
            final var textureArrayID = memory.getTextureArrays().create("Texture array %s", name);
            glBindTexture(GL_TEXTURE_2D_ARRAY, textureArrayID);
            glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, format.getInternal(), width, height, layers);

            for (int i = 0; i < layers; i++) {
                final var layerTextureInternalID = textures.get(i).getInternalID();
                glCopyImageSubData(layerTextureInternalID, GL_TEXTURE_2D, 0, 0, 0, 0,
                        textureArrayID, GL_TEXTURE_2D_ARRAY, 0, 0, 0, i,
                        width, height, 1);
            }

            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glGenerateMipmap(GL_TEXTURE_2D_ARRAY);

            return abettor.createTextureArray(textureArrayID, layers, width, height);
        });
    }

    private TextureFormat getTextureFormat(@NonNull final List<TextureEntityImplementation> textures) {
        if (textures.isEmpty())
            throw new IllegalArgumentException("Textures array can't be empty");

        final var firstFormat = textures.getFirst().getFormat();
        if (!StreamEx.of(textures).map(TextureEntityImplementation::getFormat).allMatch(format -> format == firstFormat))
            throw new IllegalArgumentException("Not all textures have the same format!");

        return firstFormat;
    }

    @Override
    protected TextureArrayEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        final var textureArrayDTO = resourceService.getAsObject(String.format("/texturearrays/%s", resourcePath), TextureArrayDTO.class);

        return create(resourcePath, StreamEx.of(textureArrayDTO.textures()).map(textureRegistry::get).toList());
    }

    @Override
    protected void unload(@NonNull final TextureArrayEntityImplementation entity) {
        log.info("Unloading texture array {}", entity.getID());

        memory.getTextureArrays().delete(entity.getInternalID());
    }
}
