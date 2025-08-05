package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.exceptions.ShaderCompileException;
import io.github.trimax.venta.engine.managers.ShaderManager;
import io.github.trimax.venta.engine.model.instance.ShaderInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ShaderInstanceImplementation;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderManagerImplementation
        extends AbstractManagerImplementation<ShaderInstanceImplementation, ShaderInstance>
        implements ShaderManager {
    @Override
    public ShaderInstanceImplementation load(@NonNull final String name,
                                             @NonNull final ShaderInstanceImplementation.Type type) {
        if (isCached(name))
            return getCached(name);

        log.info("Loading shader {}", name);

        final var code = ResourceUtil.loadAsString(String.format("/shaders/%s", name));
        final var id = glCreateShader(type.getValue());

        glShaderSource(id, code);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            throw new ShaderCompileException(glGetShaderInfoLog(id));

        return store(new ShaderInstanceImplementation(id, type, name, code));
    }

    @Override
    protected void destroy(final ShaderInstanceImplementation shader) {
        log.info("Destroying shader {} ({})", shader.getID(), shader.getName());
        glDeleteShader(shader.getInternalID());
    }

    @Override
    protected boolean shouldCache() {
        return true;
    }
}
