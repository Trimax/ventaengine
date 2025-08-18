package io.github.trimax.venta.engine.parsers;

import io.github.trimax.venta.engine.model.dto.MeshDTO;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.NonNull;

public final class JsonParsingStrategy implements AbstractParsingStrategy<MeshDTO> {
    @Override
    public MeshDTO parse(@NonNull final String resourcePath) {
        return ResourceUtil.loadAsObject(resourcePath, MeshDTO.class);
    }
}
