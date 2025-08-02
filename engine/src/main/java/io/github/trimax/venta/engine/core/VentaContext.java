package io.github.trimax.venta.engine.core;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.*;
import io.github.trimax.venta.engine.managers.implementation.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VentaContext {
    private final ResourceManagerImplementation resourceManager;
    private final MaterialManagerImplementation materialManager;
    private final TextureManagerImplementation textureManager;
    private final ProgramManagerImplementation programManager;
    private final ObjectManagerImplementation objectManager;
    private final ShaderManagerImplementation shaderManager;
    private final CameraManagerImplementation cameraManager;
    private final SceneManagerImplementation sceneManager;
    private final LightManagerImplementation lightManager;
    private final MeshManagerImplementation meshManager;
    private final FontManagerImplementation fontManager;

    @Getter(AccessLevel.PACKAGE)
    private final VentaState state = new VentaState();

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    public TextureManager getTextureManage() {
        return textureManager;
    }

    public ProgramManager getProgramManager() {
        return programManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public ShaderManager getShaderManager() {
        return shaderManager;
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

    public FontManager getFontManager() {
        return fontManager;
    }
}
