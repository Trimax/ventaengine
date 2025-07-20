package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.exceptions.ProgramLinkException;
import com.venta.engine.exceptions.ShaderArgumentException;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.dto.ProgramDTO;
import com.venta.engine.model.view.ProgramView;
import com.venta.engine.model.view.ShaderView;
import com.venta.engine.renderers.AbstractRenderer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL20C.*;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ProgramManager extends AbstractManager<ProgramManager.ProgramEntity, ProgramView> {
    private final ResourceManager resourceManager;
    private final ShaderManager shaderManager;

    public ProgramView load(final String name) {
        log.info("Loading program {}", name);

        final var programDTO = resourceManager.load(String.format("/programs/%s.json", name), ProgramDTO.class);

        final ProgramEntity program = create(programDTO.name(), StreamEx.of(programDTO.shaders()).map(shaderManager::load).toList());
        for (final String uniform : programDTO.uniforms())
            program.uniforms.put(uniform, glGetUniformLocation(program.getIdAsInteger(), uniform));

        return store(program, new ProgramView(program));
    }

    public ProgramView create(final String name, final ShaderView... shaders) {
        if (ArrayUtils.isEmpty(shaders))
            throw new ProgramLinkException(name);

        final var entity = create(name, List.of(shaders));
        return store(entity, new ProgramView(entity));
    }

    private ProgramEntity create(final String name, final List<ShaderView> shaders) {
        if (CollectionUtils.isEmpty(shaders))
            throw new ProgramLinkException(name);

        log.info("Creating program {}", name);
        final var id = glCreateProgram();

        StreamEx.of(shaders).map(AbstractRenderer.AbstractView::getId).map(Long::intValue).forEach(shaderID -> glAttachShader(id, shaderID));
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            final var message = glGetProgramInfoLog(id);
            glDeleteProgram(id);

            throw new ProgramLinkException(message);
        }

        return new ProgramEntity(id, name, shaders);
    }

    @Override
    protected void destroy(final Couple<ProgramEntity, ProgramView> program) {
        log.info("Deleting program {}", program.entity().getName());
        glDeleteProgram(program.entity().getIdAsInteger());
    }

    @Getter
    public static final class ProgramEntity extends AbstractManager.AbstractEntity {
        private final String name;

        @Getter(AccessLevel.NONE)
        private final List<ShaderView> shaders;

        @Getter(AccessLevel.NONE)
        private final Map<String, Integer> uniforms = new HashMap<>();

        ProgramEntity(final long id, @NonNull final String name, @NonNull final List<ShaderView> shaders) {
            super(id);

            this.name = name;
            this.shaders = shaders;
        }

        public int getUniformID(final String name) {
            final var uniformID = uniforms.get(name);
            if (uniformID == null)
                throw new ShaderArgumentException(String.format("%s (shader: %s)", name, this.name));

            return uniformID;
        }
    }
}
