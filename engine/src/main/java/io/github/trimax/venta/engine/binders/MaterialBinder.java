package io.github.trimax.venta.engine.binders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderMaterialUniform;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.model.entity.implementation.MaterialEntityImplementation;
import io.github.trimax.venta.engine.model.entity.implementation.ProgramEntityImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialBinder extends AbstractBinder {
    private final TextureBinder textureBinder;

    public void bind(final ProgramEntityImplementation program, final MaterialEntityImplementation material) {
        bind(program.getUniformID(ShaderUniform.UseMaterial), material != null);

        if (material == null)
            return;

        bind(program.getUniformID(ShaderUniform.MaterialMetalness), material.getMetalness());
        bind(program.getUniformID(ShaderUniform.MaterialRoughness), material.getRoughness());
        bind(program.getUniformID(ShaderUniform.MaterialTiling), material.getTiling());
        bind(program.getUniformID(ShaderUniform.MaterialOffset), material.getOffset());
        bind(program.getUniformID(ShaderUniform.MaterialColor), material.getColor());

        StreamEx.of(TextureType.values()).forEach(type -> bind(type, program, material));
    }

    public void bind(final ProgramEntityImplementation program, final MaterialEntityImplementation material, final int materialID) {
        bind(program.getUniformID(ShaderUniform.UseMaterial), material != null);

        if (material == null)
            return;

        bind(program.getUniformID(ShaderMaterialUniform.Metalness.getUniformName(materialID)), material.getMetalness());
        bind(program.getUniformID(ShaderMaterialUniform.Roughness.getUniformName(materialID)), material.getRoughness());
        bind(program.getUniformID(ShaderMaterialUniform.Tiling.getUniformName(materialID)), material.getTiling());
        bind(program.getUniformID(ShaderMaterialUniform.Offset.getUniformName(materialID)), material.getOffset());
        bind(program.getUniformID(ShaderMaterialUniform.Color.getUniformName(materialID)), material.getColor());

        StreamEx.of(TextureType.values()).forEach(type -> bind(type, program, material, materialID));
    }

    private void bind(final TextureType type, final ProgramEntityImplementation program, final MaterialEntityImplementation material) {
        textureBinder.bind(type, program, material.getTexture(type));
    }

    private void bind(final TextureType type, final ProgramEntityImplementation program, final MaterialEntityImplementation material, final int materialID) {
        textureBinder.bind(type, program, material.getTexture(type), materialID);
    }
}
