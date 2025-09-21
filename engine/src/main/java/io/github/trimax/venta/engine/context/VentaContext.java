package io.github.trimax.venta.engine.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.controllers.KeyboardController;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import io.github.trimax.venta.engine.factories.ManagerFactory;
import io.github.trimax.venta.engine.factories.RegistryFactory;
import io.github.trimax.venta.engine.factories.RepositoryFactory;
import io.github.trimax.venta.engine.managers.*;
import io.github.trimax.venta.engine.managers.implementation.*;
import io.github.trimax.venta.engine.registries.*;
import io.github.trimax.venta.engine.registries.implementation.*;
import io.github.trimax.venta.engine.repositories.*;
import io.github.trimax.venta.engine.repositories.implementation.*;
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

    public SoundSourceManager getSoundSourceManager() {
        return managerFactory.get(SoundSourceManagerImplementation.class);
    }

    public BillboardManager getBillboardManager() {
        return managerFactory.get(BillboardManagerImplementation.class);
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

    public SoundRegistry getSoundRegistry() {
        return registryFactory.get(SoundRegistryImplementation.class);
    }

    public SpriteRegistry getSpriteRegistry() {
        return registryFactory.get(SpriteRegistryImplementation.class);
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

    public SoundSourceRepository getSoundSourceRepository() {
        return repositoryFactory.get(SoundSourceRepositoryImplementation.class);
    }

    public BillboardRepository getBillboardRepository() {
        return repositoryFactory.get(BillboardRepositoryImplementation.class);
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
