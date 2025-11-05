package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.common.terrain.TerrainMaterial;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TerrainMaterialBinder extends AbstractBinder {
    private final MaterialBinder materialBinder;

    public void bind(final ProgramEntityImplementation program, @NonNull final List<TerrainMaterial> materials) {
        bind(program.getUniformID(ShaderUniform.MaterialCount), materials.size());

        int materialID = 0;
        for (final var material : materials)
            bind(program, material, materialID++);
    }

    private void bind(final ProgramEntityImplementation program, final TerrainMaterial material, final int materialIndex) {
        if (material.getMaterial() instanceof MaterialEntityImplementation entity)
            materialBinder.bind(program, entity, materialIndex);
    }
}
