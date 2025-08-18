package io.github.trimax.venta.engine.model.parameters;

import io.github.trimax.venta.engine.model.entity.MaterialEntity;
import io.github.trimax.venta.engine.model.entity.MeshEntity;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectParameters implements AbstractParameters {
    @NonNull
    MeshEntity mesh;

    @NonNull
    ProgramEntity program;

    @NonNull
    MaterialEntity material;
}
