package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.CubemapFace;
import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.exceptions.CubemapBakeException;
import io.github.trimax.venta.engine.helpers.GeometryHelper;
import io.github.trimax.venta.engine.layouts.CubemapVertexLayout;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.dto.CubemapDTO;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.CubemapEntityImplementation;
import io.github.trimax.venta.engine.registries.CubemapRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;

import static io.github.trimax.venta.engine.definitions.GeometryDefinitions.SKYBOX_VERTICES;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.system.MemoryStack.stackPush;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CubemapRegistryImplementation
        extends AbstractRegistryImplementation<CubemapEntityImplementation, CubemapEntity, Void>
        implements CubemapRegistry {
    private final ProgramRegistryImplementation programRegistry;
    private final ResourceService resourceService;
    private final GeometryHelper geometryHelper;
    private final Abettor abettor;
    private final Memory memory;

    @Override
    protected CubemapEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        return load(resourcePath, resourceService.getAsObject(String.format("/cubemaps/%s", resourcePath), CubemapDTO.class));
    }

    private CubemapEntityImplementation load(@NonNull final String resourcePath, final CubemapDTO dto) {
        return get(resourcePath, () -> {
            final int textureID = memory.getTextures().create("3D texture %s", resourcePath);
            glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);

            final var buffers = StreamEx.of(CubemapFace.values())
                    .mapToEntry(Function.identity(), face -> loadTextureSafe(face, dto.getTexturePath(face)))
                    .filterValues(Objects::nonNull)
                    .toMap();

            if (buffers.size() != CubemapFace.values().length) {
                StreamEx.ofValues(buffers).forEach(MemoryUtil::memFree);
                throw new CubemapBakeException("Cubemap can't be constructed. Some textures can't be loaded");
            }

            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

            glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

            return abettor.createCubemap(buffers, programRegistry.get(dto.program()), TextureFormat.RGB,
                    geometryHelper.create(resourcePath, CubemapVertexLayout.class, SKYBOX_VERTICES, null, null),
                    textureID);
        });
    }

    private ByteBuffer loadTextureSafe(final CubemapFace face, final String path) {
        try {
            return loadTexture(face, path);
        } catch (final Exception ignored) {
            log.warn("Can't load texture {}", path);
        }

        return null;
    }

    private ByteBuffer loadTexture(final CubemapFace face, final String path) {
        final byte[] data = resourceService.getAsBytes("/textures/" + path);

        try (var stack = stackPush()) {
            final var widthBuffer = stack.mallocInt(1);
            final var heightBuffer = stack.mallocInt(1);
            final var channelsBuffer = stack.mallocInt(1);

            final var imageBuffer = BufferUtils.createByteBuffer(data.length);
            imageBuffer.put(data).flip();

            final ByteBuffer pixels = STBImage.stbi_load_from_memory(imageBuffer, widthBuffer, heightBuffer, channelsBuffer, 0);
            if (pixels == null) {
                MemoryUtil.memFree(imageBuffer);
                return null;
            }

            final var width = widthBuffer.get(0);
            final var height = heightBuffer.get(0);

            final var format = TextureFormat.of(channelsBuffer.get(0));

            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face.getIndex(), 0, format.getInternal(), width, height, 0, format.getExternal(), GL_UNSIGNED_BYTE, pixels);
            STBImage.stbi_image_free(pixels);

            return imageBuffer;
        }
    }

    @Override
    protected void unload(@NonNull final CubemapEntityImplementation entity) {
        log.info("Unloading cubemap {}", entity.getID());

        geometryHelper.delete(entity.getGeometry());
        memory.getTextures().delete(entity.getInternalID());
        StreamEx.ofValues(entity.getBuffers()).forEach(MemoryUtil::memFree);
    }
}
