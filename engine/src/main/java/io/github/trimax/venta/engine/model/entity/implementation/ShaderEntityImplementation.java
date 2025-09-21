package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.model.entity.ShaderEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ShaderEntityImplementation extends AbstractEntityImplementation implements ShaderEntity {
    int internalID;

    @NonNull
    ShaderType type;

    @NonNull
    String code;
}