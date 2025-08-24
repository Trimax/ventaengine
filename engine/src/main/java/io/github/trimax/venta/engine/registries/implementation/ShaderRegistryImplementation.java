package io.github.trimax.venta.engine.registries.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ShaderType;
import io.github.trimax.venta.engine.exceptions.ShaderCompileException;
import io.github.trimax.venta.engine.model.entity.ShaderEntity;
import io.github.trimax.venta.engine.model.entity.implementation.ShaderEntityImplementation;
import io.github.trimax.venta.engine.registries.ShaderRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderRegistryImplementation
        extends AbstractRegistryImplementation<ShaderEntityImplementation, ShaderEntity, ShaderType>
        implements ShaderRegistry {
    private final ResourceService resourceService;

    @Override
    protected ShaderEntityImplementation load(@NonNull final String resourcePath, final ShaderType type) {
        log.info("Loading shader {}", resourcePath);

        final var code = resourceService.getAsString(String.format("/shaders/%s", resourcePath));
        final var id = glCreateShader(type.getValue());

        glShaderSource(id, code);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            throw new ShaderCompileException(glGetShaderInfoLog(id));

        return new ShaderEntityImplementation(id, type, code);
    }

    @Override
    protected void unload(@NonNull final ShaderEntityImplementation entity) {
        log.info("Unloading shader {}", entity.getID());
        glDeleteShader(entity.getInternalID());
    }
}
