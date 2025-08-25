package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.CubemapFace;
import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class CubemapEntityImplementation extends AbstractEntityImplementation implements CubemapEntity {
    private final Map<CubemapFace, ByteBuffer> buffers;
    private final ProgramEntityImplementation program;
    private final TextureFormat format;
    private final int internalID;
}
