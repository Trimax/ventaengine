package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.dto.MaterialDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.lwjgl.stb.STBTTBakedChar;

import java.nio.ByteBuffer;

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

    public MeshEntityImplementation createMesh(final int verticesCount,
                                               final int facetsCount,
                                               final int edgesCount,
                                               final int vertexArrayObjectID,
                                               final int vertexBufferID,
                                               final int facetsBufferID,
                                               final int edgesBufferID,
                                               @NonNull final BoundingBox boundingBox) {
        return new MeshEntityImplementation(verticesCount, facetsCount, edgesCount,
                vertexArrayObjectID, vertexBufferID, facetsBufferID, edgesBufferID, boundingBox);
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
}
