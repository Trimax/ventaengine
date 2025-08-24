package io.github.trimax.venta.engine.parsers;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.dto.MeshDTO;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@Component
@SuppressWarnings("unused")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonMeshParser implements AbstractParser<MeshDTO> {
    private final ResourceService resourceService;

    @Override
    public MeshDTO parse(@NonNull final String resourcePath) {
        return resourceService.getAsObject(resourcePath, MeshDTO.class);
    }

    @Override
    public MeshFormat format() {
        return MeshFormat.JSON;
    }
}
