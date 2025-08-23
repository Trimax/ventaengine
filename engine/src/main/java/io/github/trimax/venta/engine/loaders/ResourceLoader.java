package io.github.trimax.venta.engine.loaders;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.DataType;
import io.github.trimax.venta.engine.registries.implementation.ShaderRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceLoader {
    private final TextureRegistryImplementation textureRegistry;
    private final ShaderRegistryImplementation shaderRegistry;

    //TODO: Implement other options
    public void load(final String path, final DataType type, final byte[] data) {
        log.debug("Resource read: {} ({}). Length: {}", path, type, data.length);

        switch (type) {
            case Textures:
                textureRegistry.load(new Resource(type.shortenPath(path), data));
                return;
            case Materials:
                //Question: material.get will call texture.get. But should call texture.load. How to? Register archives
                //          and check where is the resource? Think about it
                return;
            case Shaders:
                log.info("Loading shader: {}", path);
                return;
        }
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Resource {
        String path;
        byte[] data;
    }
}
