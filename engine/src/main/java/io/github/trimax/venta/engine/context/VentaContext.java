package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.*;
import io.github.trimax.venta.engine.managers.implementation.*;
import io.github.trimax.venta.engine.registries.FontRegistry;
import io.github.trimax.venta.engine.registries.ProgramRegistry;
import io.github.trimax.venta.engine.registries.ShaderRegistry;
import io.github.trimax.venta.engine.registries.TextureRegistry;
import io.github.trimax.venta.engine.registries.implementation.FontRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ShaderRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VentaContext {
    private final MaterialManagerImplementation materialManager;
    private final ObjectManagerImplementation objectManager;
    private final CameraManagerImplementation cameraManager;
    private final SceneManagerImplementation sceneManager;
    private final LightManagerImplementation lightManager;
    private final MeshManagerImplementation meshManager;

    private final TextureRegistryImplementation textureRegistry;
    private final ProgramRegistryImplementation programRegistry;
    private final ShaderRegistryImplementation shaderRegistry;
    private final FontRegistryImplementation fontRegistry;

    /*** Managers ***/

    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public LightManager getLightManager() {
        return lightManager;
    }

    public MeshManager getMeshManager() {
        return meshManager;
    }

    /*** Registries ***/

    public TextureRegistry getTextureRegistry() {
        return textureRegistry;
    }

    public ProgramRegistry getProgramRegistry() {
        return programRegistry;
    }

    public ShaderRegistry getShaderRegistry() {
        return shaderRegistry;
    }

    public FontRegistry getFontRegistry() {
        return fontRegistry;
    }
}
