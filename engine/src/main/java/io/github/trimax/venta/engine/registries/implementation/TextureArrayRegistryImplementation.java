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
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.glTexImage3D;
import static org.lwjgl.opengl.GL12C.glTexSubImage3D;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

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
            if (!StreamEx.of(textures).allMatch(texture -> texture.getWidth() == width && texture.getHeight() == height))
                throw new IllegalArgumentException(String.format("Not all textures of the texture array %s have the same size %dx%d", name, width, height));

            final var textureArrayID = memory.getTextureArrays().create("Texture array %s", name);
            glBindTexture(GL_TEXTURE_2D_ARRAY, textureArrayID);
            glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, format.getInternal(), width, height, layers, 0, format.getExternal(), GL_UNSIGNED_BYTE, (ByteBuffer) null);

            // Layers are copied through the client memory: glCopyImageSubData requires GL 4.3, which is not available on macOS (GL 4.1)
            glPixelStorei(GL_PACK_ALIGNMENT, 1);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            final var pixels = MemoryUtil.memAlloc(width * height * format.getChannelsCount());
            try {
                for (int i = 0; i < layers; i++) {
                    glBindTexture(GL_TEXTURE_2D, textures.get(i).getInternalID());
                    glGetTexImage(GL_TEXTURE_2D, 0, format.getExternal(), GL_UNSIGNED_BYTE, pixels);
                    glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1, format.getExternal(), GL_UNSIGNED_BYTE, pixels);
                }
            } finally {
                MemoryUtil.memFree(pixels);
                glBindTexture(GL_TEXTURE_2D, 0);
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
