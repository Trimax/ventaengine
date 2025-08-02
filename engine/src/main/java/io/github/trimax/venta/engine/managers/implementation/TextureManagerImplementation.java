package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.exceptions.UnknownTextureFormatException;
import io.github.trimax.venta.engine.model.view.TextureView;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_HEIGHT;
import static io.github.trimax.venta.engine.definitions.Definitions.FONT_ATLAS_WIDTH;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
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
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.stackPush;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureManagerImplementation extends AbstractManagerImplementation<TextureManagerImplementation.TextureEntity, TextureView> {
    private final ResourceManagerImplementation resourceManager;

    public TextureView create(@NonNull final String name, @NonNull final ByteBuffer bitmap) {
        final var textureID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureID);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        return store(new TextureEntity(textureID, name, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT));
    }

    public TextureView load(@NonNull final String name) {
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

    @Override
    protected boolean shouldCache() {
        return true;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Texture;
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
