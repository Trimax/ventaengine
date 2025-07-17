package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ResourceManager extends AbstractManager<ResourceManager.ResourceEntity> {
    static final ResourceManager instance = new ResourceManager();

    public String load(final String path) {
        try (final InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }

    @Override
    protected void destroy(final ResourceEntity value) {
        log.info("Destroying resource: {}", value.getId());
    }

    @Getter
    public static final class ResourceEntity extends AbstractEntity {
        ResourceEntity(final long id) {
            super(id);
        }
    }
}
