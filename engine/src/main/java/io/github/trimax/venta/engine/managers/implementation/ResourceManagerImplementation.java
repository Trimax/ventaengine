package io.github.trimax.venta.engine.managers.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.adapters.TextureTypeAdapter;
import io.github.trimax.venta.engine.enums.EntityType;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.exceptions.ResourceNotFoundException;
import io.github.trimax.venta.engine.model.view.ResourceView;
import io.github.trimax.venta.engine.utils.ResourceUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceManagerImplementation extends AbstractManagerImplementation<ResourceManagerImplementation.ResourceEntity, ResourceView> {
    private static final Gson parser = new GsonBuilder()
            .registerTypeAdapter(TextureType.class, new TextureTypeAdapter())
            .create();

    public ByteBuffer loadAsBuffer(final String path) {
        log.debug("Loading resource buffer: {}", path);
        final var bytes = loadAsBytes(path);

        final var buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        return buffer;
    }

    public byte[] loadAsBytes(final String path) {
        log.debug("Loading resource bytes: {}", path);

        try (final InputStream stream = ResourceManagerImplementation.class.getResourceAsStream(path)) {
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

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Resource;
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
