package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.KeyboardController;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.factories.ManagerFactory;
import io.github.trimax.venta.engine.factories.RegistryFactory;
import io.github.trimax.venta.engine.factories.RepositoryFactory;
import io.github.trimax.venta.engine.managers.CameraManager;
import io.github.trimax.venta.engine.managers.EmitterManager;
import io.github.trimax.venta.engine.managers.GridMeshManager;
import io.github.trimax.venta.engine.managers.LightManager;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.managers.SceneManager;
import io.github.trimax.venta.engine.managers.implementation.CameraManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.EmitterManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.GridMeshManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.LightManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.ObjectManagerImplementation;
import io.github.trimax.venta.engine.managers.implementation.SceneManagerImplementation;
import io.github.trimax.venta.engine.registries.CubemapRegistry;
import io.github.trimax.venta.engine.registries.FontRegistry;
import io.github.trimax.venta.engine.registries.MaterialRegistry;
import io.github.trimax.venta.engine.registries.MeshRegistry;
import io.github.trimax.venta.engine.registries.ProgramRegistry;
import io.github.trimax.venta.engine.registries.ShaderRegistry;
import io.github.trimax.venta.engine.registries.TextureRegistry;
import io.github.trimax.venta.engine.registries.implementation.CubemapRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.FontRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.MaterialRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.MeshRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.ShaderRegistryImplementation;
import io.github.trimax.venta.engine.registries.implementation.TextureRegistryImplementation;
import io.github.trimax.venta.engine.repositories.EmitterRepository;
import io.github.trimax.venta.engine.repositories.GridMeshRepository;
import io.github.trimax.venta.engine.repositories.LightRepository;
import io.github.trimax.venta.engine.repositories.ObjectRepository;
import io.github.trimax.venta.engine.repositories.SceneRepository;
import io.github.trimax.venta.engine.repositories.implementation.EmitterRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.GridMeshRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.LightRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.ObjectRepositoryImplementation;
import io.github.trimax.venta.engine.repositories.implementation.SceneRepositoryImplementation;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.NonNull;
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
    private final ResourceService resourceService;

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

    public EmitterManager getEmitterManager() {
        return managerFactory.get(EmitterManagerImplementation.class);
    }

    public GridMeshManager getGridMeshManager() {
        return managerFactory.get(GridMeshManagerImplementation.class);
    }

    /*** Registries ***/

    public CubemapRegistry getCubemapRegistry() {
        return registryFactory.get(CubemapRegistryImplementation.class);
    }

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

    public EmitterRepository getEmitterRepository() {
        return repositoryFactory.get(EmitterRepositoryImplementation.class);
    }

    public GridMeshRepository getGridMeshRepository() {
        return repositoryFactory.get(GridMeshRepositoryImplementation.class);
    }

    /*** Keyboard ***/

    public boolean isButtonPushed(final int key) {
        return controllerFactory.get(KeyboardController.class).get().isButtonPushed(key);
    }

    /*** Resources ***/

    public void registerArchive(@NonNull final String archiveFilePath) {
        resourceService.loadArchive(archiveFilePath);
    }
}
