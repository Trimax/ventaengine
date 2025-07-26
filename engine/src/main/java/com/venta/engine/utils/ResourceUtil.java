package com.venta.engine.utils;

import com.venta.engine.exceptions.ResourceNotFoundException;
import com.venta.engine.managers.ResourceManager;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@UtilityClass
public final class ResourceUtil {
    public String load(final String path) {
        try (final InputStream stream = ResourceManager.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new ResourceNotFoundException(path);

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new ResourceNotFoundException(path);
        }
    }
}
