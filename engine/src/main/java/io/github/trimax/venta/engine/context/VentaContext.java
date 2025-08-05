package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.managers.FontManager;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.managers.MaterialManager;
import io.github.trimax.venta.engine.managers.MeshManager;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.managers.ProgramManager;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.managers.ShaderManager;
import io.github.trimax.venta.engine.managers.TextureManager;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.FontManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.LightManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.MaterialManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.MeshManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ProgramManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ShaderManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.TextureManagerImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VentaContext {
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
