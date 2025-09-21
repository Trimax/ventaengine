package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.entity.ProgramEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ProgramEntityImplementation  extends AbstractEntityImplementation implements ProgramEntity {
    int internalID;

    @Getter(AccessLevel.NONE)
    Map<String, Integer> uniforms = new HashMap<>();

    public void addUniformID(final String name, final Integer uniformID) {
        if (uniformID >= 0)
            this.uniforms.put(name, uniformID);
    }

    public int getUniformID(final String name) {
        return uniforms.getOrDefault(name, -1);
    }

    public int getUniformID(final ShaderUniform uniform) {
        return getUniformID(uniform.getUniformName());
    }

    public int getUniformCount() {
        return uniforms.size();
    }
}
