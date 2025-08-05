package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.model.entity.ShaderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ShaderEntityImplementation extends AbstractEntityImplementation implements ShaderEntity {
    private final int internalID;

    @NonNull
    private final ShaderType type;

    @NonNull
    private final String code;
}