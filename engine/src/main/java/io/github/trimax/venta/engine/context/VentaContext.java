package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.KeyboardController;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.factories.ManagerFactory;
import io.github.trimax.venta.engine.factories.RegistryFactory;
import io.github.trimax.venta.engine.factories.RepositoryFactory;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.LightManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.registries.*;
import io.github.trimax.venta.engine.registries.implementation.*;
import io.github.trimax.venta.engine.repositories.LightRepository;
import io.github.trimax.venta.engine.repositories.ObjectRepository;
import io.github.trimax.venta.engine.repositories.SceneRepository;
import io.github.trimax.venta.engine.repositories.implementation.LightRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.ObjectRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.SceneRepositoryImplementation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VentaContext {
    private final ControllerFactory controllerFactory;
    private final RepositoryFactory repositoryFactory;
    private final RegistryFactory registryFactory;
    private final ManagerFactory managerFactory;

    /*** Managers ***/

    public ObjectManager getObjectManager() {
        return managerFactory.get(ObjectManagerImplementation.class);
    }

    public CameraManager getCameraManager() {
        return managerFactory.get(CameraManagerImplementation.class);
    }

    public SceneManager getSceneManager() {
        return managerFactory.get(SceneManagerImplementation.class);
    }

    public LightManager getLightManager() {
        return managerFactory.get(LightManagerImplementation.class);
    }

    /*** Registries ***/

    public MaterialRegistry getMaterialRegistry() {
        return registryFactory.get(MaterialRegistryImplementation.class);
    }

    public TextureRegistry getTextureRegistry() {
        return registryFactory.get(TextureRegistryImplementation.class);
    }

    public ProgramRegistry getProgramRegistry() {
        return registryFactory.get(ProgramRegistryImplementation.class);
    }

    public ShaderRegistry getShaderRegistry() {
        return registryFactory.get(ShaderRegistryImplementation.class);
    }

    public FontRegistry getFontRegistry() {
        return registryFactory.get(FontRegistryImplementation.class);
    }

    public MeshRegistry getMeshRegistry() {
        return registryFactory.get(MeshRegistryImplementation.class);
    }

    /*** Repositories ***/

    public ObjectRepository getObjectRepository() {
        return repositoryFactory.get(ObjectRepositoryImplementation.class);
    }

    public LightRepository getLightRepository() {
        return repositoryFactory.get(LightRepositoryImplementation.class);
    }

    public SceneRepository getSceneRepository() {
        return repositoryFactory.get(SceneRepositoryImplementation.class);
    }

    /*** Keyboard ***/
    public boolean isButtonPushed(final int key) {
        return controllerFactory.get(KeyboardController.class).get().isButtonPushed(key);
    }
}
