package com.venta.engine.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venta.engine.annotations.Component;
import com.venta.engine.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ResourceManager extends AbstractManager<ResourceManager.ResourceEntity> {
    private static final Gson parser = new GsonBuilder().create();

    public String load(final String path) {
        log.debug("Loading resource: {}", path);
        try (final InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }

    public <O> O load(final String path, final Class<O> objectClass) {
        return parser.fromJson(load(path), objectClass);
    }

    @Override
    protected void destroy(final ResourceEntity value) {
        log.debug("Deleting resource: {}", value.getId());
    }

    @Getter
    public static final class ResourceEntity extends AbstractEntity {
        ResourceEntity(final long id) {
            super(id);
        }
    }
}
