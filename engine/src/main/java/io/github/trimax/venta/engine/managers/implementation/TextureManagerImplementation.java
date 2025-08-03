package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.UnknownTextureFormatException;
import io.github.trimax.venta.engine.managers.TextureManager;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.TextureEntity;
import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_HEIGHT;
import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_WIDTH;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_RED;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureManagerImplementation
        extends AbstractManagerImplementation<TextureEntity, TextureView>
        implements TextureManager {
    private final ResourceManagerImplementation resourceManager;
    private final Memory memory;

    public TextureEntity create(@NonNull final String name,
                                @NonNull final ByteBuffer bitmap) {
        final var glTexture = memory.getTextures().create("Texture %s", name);

        glBindTexture(GL_TEXTURE_2D, glTexture.getData());
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        return store(new TextureEntity(name, glTexture, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT));
    }

    @Override
    public TextureView load(@NonNull final String name) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading texture {}", name);

        final byte[] imageData = resourceManager.loadAsBytes(String.format("/textures/%s", name));
        try (var stack = stackPush()) {
            final var widthBuffer = stack.mallocInt(1);
            final var heightBuffer = stack.mallocInt(1);
            final var channelsBuffer = stack.mallocInt(1);

            final var imageBuffer = memory.getByteBuffers().create(() -> BufferUtils.createByteBuffer(imageData.length), "Byte buffer %s", name);
            imageBuffer.getData().put(imageData);
            imageBuffer.getData().flip();

            final var pixels = STBImage.stbi_load_from_memory(imageBuffer.getData(), widthBuffer, heightBuffer, channelsBuffer, 4);
            if (pixels == null) {
                memory.getByteBuffers().delete(imageBuffer);
                throw new UnknownTextureFormatException(String.format("%s (%s)", name, STBImage.stbi_failure_reason()));
            }

            final var width = widthBuffer.get(0);
            final var height = heightBuffer.get(0);

            final var glTexture = memory.getTextures().create("Texture %s", name);
            glBindTexture(GL_TEXTURE_2D, glTexture.getData());

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);

            STBImage.stbi_image_free(pixels);
            memory.getByteBuffers().delete(imageBuffer);

            return store(new TextureEntity(name, glTexture, width, height));
        }
    }

    @Override
    protected void destroy(final TextureEntity texture) {
        log.info("Destroying texture {} ({})", texture.getID(), texture.getName());
        memory.getTextures().delete(texture.getInternal());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
