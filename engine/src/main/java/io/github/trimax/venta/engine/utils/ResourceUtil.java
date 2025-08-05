package io.github.trimax.venta.engine.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.trimax.venta.engine.adapters.TextureTypeAdapter;
import io.github.trimax.venta.engine.enums.TextureType;
import io.github.trimax.venta.engine.exceptions.ResourceNotFoundException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class ResourceUtil {
    private static final Gson parser = new GsonBuilder()
            .registerTypeAdapter(TextureType.class, new TextureTypeAdapter())
            .create();

    public ByteBuffer loadAsBuffer(final String path) {
        log.debug("Loading resource as byte buffer: {}", path);
        final var bytes = loadAsBytes(path);

        final var buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        return buffer;
    }

    public byte[] loadAsBytes(final String path) {
        log.debug("Loading resource byte array: {}", path);

        try (final InputStream stream = ResourceUtil.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return stream.readAllBytes();
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }

    public <O> O loadAsObject(final String path, final Class<O> objectClass) {
        return parser.fromJson(loadAsString(path), objectClass);
    }

    public String loadAsString(final String path) {
        try (final InputStream stream = ResourceUtil.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }
}
