package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.CubemapFace;
import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.joml.Vector4f;
import org.lwjgl.stb.STBTTBakedChar;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Abettor {
    public AtlasEntityImplementation createAtlas(@NonNull final TextureEntityImplementation texture,
                                                 @NonNull final STBTTBakedChar.Buffer buffer) {
        return new AtlasEntityImplementation(texture, buffer);
    }

    public FontEntityImplementation createFont(@NonNull final ByteBuffer buffer) {
        return new FontEntityImplementation(buffer);
    }

    public MaterialEntityImplementation createMaterial(@NonNull final MaterialDTO materialDTO) {
        return new MaterialEntityImplementation(materialDTO);
    }

    public GridMeshEntityImplementation createGridMesh(@NonNull final Geometry geometry) {
        return new GridMeshEntityImplementation(geometry);
    }

    public MeshEntityImplementation createMesh(@NonNull final Geometry geometry,
                                               @NonNull final BoundingBox boundingBox) {
        return new MeshEntityImplementation(geometry, boundingBox);
    }

    public ProgramEntityImplementation createProgram(final int internalID) {
        return new ProgramEntityImplementation(internalID);
    }

    public ShaderEntityImplementation createShader(final int internalID,
                                                   @NonNull final ShaderType type,
                                                   @NonNull final String code) {
        return new ShaderEntityImplementation(internalID, type, code);
    }

    public TextureEntityImplementation createTexture(@NonNull final ByteBuffer buffer,
                                                     @NonNull final TextureFormat format,
                                                     final int internalID,
                                                     final int width,
                                                     final int height) {
        return new TextureEntityImplementation(buffer, format, internalID, width, height);
    }

    public CubemapEntityImplementation createCubemap(@NonNull final Map<CubemapFace, ByteBuffer> buffers,
                                                     @NonNull final ProgramEntityImplementation program,
                                                     @NonNull final TextureFormat format,
                                                     @NonNull final Geometry geometry,
                                                     final int internalID) {
        return new CubemapEntityImplementation(buffers, program, format, geometry, internalID);
    }

    public SoundEntityImplementation createSound(final int bufferID, final float duration) {
        return new SoundEntityImplementation(bufferID, duration);
    }

    public SpriteEntityImplementation createSprite(@NonNull final TextureEntityImplementation texture,
                                                   @NonNull final FloatBuffer frames,
                                                   @NonNull final Vector4f color,
                                                   final boolean looping,
                                                   final int frameCount,
                                                   final float duration) {
        return new SpriteEntityImplementation(texture, frames, color, looping, frameCount, duration);
    }
}
