package com.venta.engine.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.venta.engine.adapters.TextureTypeAdapter;
import com.venta.container.annotations.Component;
import com.venta.engine.enums.TextureType;
import com.venta.engine.exceptions.ResourceNotFoundException;
import com.venta.engine.model.view.ResourceView;
import com.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
        return ResourceUtil.load(path);
    }

    public <O> O load(final String path, final Class<O> objectClass) {
        return parser.fromJson(load(path), objectClass);
    }

    @Override
    protected void destroy(final ResourceEntity resource) {
        log.debug("Destroying resource {} ({})", resource.getID(), resource.getName());
    }

    @Getter
    public static final class ResourceEntity extends AbstractEntity implements ResourceView {
        ResourceEntity() {
            super(UUID.randomUUID().toString());
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ResourceAccessor extends AbstractAccessor {}
}
