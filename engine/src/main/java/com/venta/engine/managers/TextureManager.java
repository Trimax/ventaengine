package com.venta.engine.managers;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import com.venta.container.annotations.Component;
import com.venta.engine.exceptions.UnknownTextureFormatException;
import com.venta.engine.model.view.TextureView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureManager extends AbstractManager<TextureManager.TextureEntity, TextureView> {
    private final ResourceManager resourceManager;

    public TextureView load(final String name) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading texture {}", name);

        final byte[] imageData = resourceManager.loadAsBytes(String.format("/textures/%s", name));
        try (var stack = stackPush()) {
            final var widthBuffer = stack.mallocInt(1);
            final var heightBuffer = stack.mallocInt(1);
            final var channelsBuffer = stack.mallocInt(1);

            final var imageBuffer = BufferUtils.createByteBuffer(imageData.length);
            imageBuffer.put(imageData);
            imageBuffer.flip();

            final var pixels = STBImage.stbi_load_from_memory(imageBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (pixels == null) {
                MemoryUtil.memFree(imageBuffer);
                throw new UnknownTextureFormatException(String.format("%s (%s)", name, STBImage.stbi_failure_reason()));
            }

            final var width = widthBuffer.get(0);
            final var height = heightBuffer.get(0);

            final int textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);

            STBImage.stbi_image_free(pixels);

            return store(new TextureEntity(textureID, name, width, height));
        }
    }

    @Override
    protected void destroy(final TextureEntity texture) {
        log.info("Destroying texture {} ({})", texture.getID(), texture.getName());
        glDeleteTextures(texture.getInternalID());
    }

    @Getter
    public static final class TextureEntity extends AbstractEntity implements TextureView {
        private final int internalID;
        private final int width;
        private final int height;

        TextureEntity(final int internalID,
                @NonNull final String name,
                final int width,
                final int height) {
            super(name);

            this.internalID = internalID;
            this.width = width;
            this.height = height;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class TextureAccessor extends AbstractAccessor {}
}
