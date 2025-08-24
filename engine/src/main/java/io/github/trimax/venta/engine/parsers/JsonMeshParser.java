package io.github.trimax.venta.engine.parsers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.dto.MeshDTO;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonMeshParser implements AbstractParser<MeshDTO> {
    @Override
    public MeshDTO parse(@NonNull final String resourcePath) {
        return ResourceUtil.loadAsObject(resourcePath, MeshDTO.class);
    }

    @Override
    public MeshFormat format() {
        return MeshFormat.JSON;
    }
}
