package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.model.entity.ShaderEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ShaderEntityImplementation extends AbstractEntityImplementation implements ShaderEntity {
    private final int internalID;

    @NonNull
    private final ShaderType type;

    @NonNull
    private final String code;
}