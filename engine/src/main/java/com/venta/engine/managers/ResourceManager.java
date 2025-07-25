package com.venta.engine.managers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venta.engine.adapters.TextureTypeAdapter;
import com.venta.engine.annotations.Component;
import com.venta.engine.enums.TextureType;
import com.venta.engine.exceptions.ResourceNotFoundException;
import com.venta.engine.model.core.Couple;
import com.venta.engine.model.view.ResourceView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ResourceManager extends AbstractManager<ResourceManager.ResourceEntity, ResourceView> {
    private static final Gson parser = new GsonBuilder()
            .registerTypeAdapter(TextureType.class, new TextureTypeAdapter())
            .create();

    public byte[] loadAsBytes(final String path) {
        log.debug("Loading resource bytes: {}", path);

        try (final InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return stream.readAllBytes();
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }

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
    protected ResourceView createView(final String id, final ResourceEntity entity) {
        return new ResourceView(id, entity);
    }

    @Override
    protected void destroy(final Couple<ResourceEntity, ResourceView> resource) {
        log.debug("Deleting resource: {}", resource.entity().getId());
    }

    @Getter
    public static final class ResourceEntity extends AbstractEntity {
        ResourceEntity(final long id) {
            super(id);
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ResourceAccessor extends AbstractAccessor {}
}
