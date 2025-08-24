package io.github.trimax.venta.engine.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.trimax.venta.engine.adapters.TextureTypeAdapter;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.exceptions.ResourceNotFoundException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@UtilityClass
public final class ResourceUtil {
    private static final Gson parser = new GsonBuilder()
            .registerTypeAdapter(TextureType.class, new TextureTypeAdapter())
            .create();

    public byte[] loadAsBytes(@NonNull final String path) {
        log.debug("Loading resource byte array: {}", path);

        try (final InputStream stream = ResourceUtil.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return stream.readAllBytes();
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }

    public <O> O loadAsObject(@NonNull final String path, @NonNull final Class<O> objectClass) {
        return parser.fromJson(loadAsString(path), objectClass);
    }

    public <O> O loadAsObject(@NonNull final byte[] data, @NonNull final Class<O> objectClass) {
        return loadAsObject(new String(data, StandardCharsets.UTF_8), objectClass);
    }

    public String loadAsString(@NonNull final String path) {
        try (final InputStream stream = ResourceUtil.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }
}
