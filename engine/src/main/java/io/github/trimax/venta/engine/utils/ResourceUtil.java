package io.github.trimax.venta.engine.utils;

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
