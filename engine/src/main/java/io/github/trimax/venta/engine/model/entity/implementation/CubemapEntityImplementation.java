package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.CubemapFace;
import io.github.trimax.venta.engine.enums.TextureFormat;
import io.github.trimax.venta.engine.model.common.geo.Geometry;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.nio.ByteBuffer;
import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class CubemapEntityImplementation extends AbstractEntityImplementation implements CubemapEntity {
    @NonNull
    Map<CubemapFace, ByteBuffer> buffers;

    @NonNull
    ProgramEntityImplementation program;

    @NonNull
    TextureFormat format;

    @NonNull
    Geometry geometry;

    int internalID;
}
