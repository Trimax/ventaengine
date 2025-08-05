package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.model.instance.ProgramInstance;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class ProgramInstanceImplementation extends AbstractInstanceImplementation implements ProgramInstance {
    private final int internalID;

    @Getter(AccessLevel.NONE)
    private final Map<String, Integer> uniforms = new HashMap<>();

    public ProgramInstanceImplementation(final int internalID, @NonNull final String name) {
        super(name);

        this.internalID = internalID;
    }

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
