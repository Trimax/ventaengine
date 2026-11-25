package io.github.trimax.venta.engine.model.instance;

import io.github.trimax.venta.engine.model.common.shared.DirectionalLight;
import io.github.trimax.venta.engine.enums.ToneMapping;
import io.github.trimax.venta.engine.model.common.shared.Fog;
import io.github.trimax.venta.engine.model.common.shared.Bloom;
import io.github.trimax.venta.engine.model.common.shared.AmbientOcclusion;
import io.github.trimax.venta.engine.model.common.shared.Antialiasing;
import io.github.trimax.venta.engine.model.common.shared.View;
import io.github.trimax.venta.engine.model.common.shared.LightShafts;
import io.github.trimax.venta.engine.model.common.shared.Wind;
import io.github.trimax.venta.engine.model.entity.CubemapEntity;
import lombok.NonNull;
import org.joml.Vector3fc;

import java.util.Collection;

public interface SceneInstance extends AbstractInstance {
    Collection<? extends TerrainSurfaceInstance> getTerrainSurfaces();

    Collection<? extends WaterSurfaceInstance> getWaterSurfaces();

    Collection<? extends SoundSourceInstance> getSoundSources();

    Collection<? extends BillboardInstance> getBillboards();

    Collection<? extends EmitterInstance> getEmitters();

    Collection<? extends ObjectInstance> getObjects();

    Collection<? extends LightInstance> getLights();

    DirectionalLight getDirectionalLight();

    Vector3fc getAmbientLight();

    CubemapEntity getSkybox();

    Fog getFog();

    /** The wind that sways the surfaces which follow it, null is a calm */
    Wind getWind();

    /** The glow around the bright parts of the image, null is no bloom */
    Bloom getBloom();

    /** The shafts of the light of the sun in the fog, null is none */
    LightShafts getLightShafts();

    /** The darkening of the corners and the contacts, null is none */
    AmbientOcclusion getAmbientOcclusion();

    /** The smoothing of the edges of the finished image, null is none */
    Antialiasing getAntialiasing();

    /** How the camera sees the scene: the planes and the field of view, null is the default view */
    View getView();

    /** Multiplies the light of the scene before the tone mapping, 1 keeps it */
    float getExposure();

    ToneMapping getToneMapping();

    void setDirectionalLight(@NonNull final DirectionalLight directionalLight);

    void setAmbientLight(@NonNull final Vector3fc ambientLight);

    void setSkybox(final CubemapEntity skybox);

    void setFog(final Fog fog);

    void setWind(final Wind wind);

    void setBloom(final Bloom bloom);

    void setLightShafts(final LightShafts lightShafts);

    void setAmbientOcclusion(final AmbientOcclusion ambientOcclusion);

    void setAntialiasing(final Antialiasing antialiasing);

    void setView(final View view);

    void setExposure(final float exposure);

    void setToneMapping(@NonNull final ToneMapping toneMapping);

    void add(@NonNull final LightInstance light);

    void add(@NonNull final ObjectInstance object);

    void add(@NonNull final EmitterInstance emitter);

    void add(@NonNull final SoundSourceInstance sound);

    void add(@NonNull final BillboardInstance billboard);

    void add(@NonNull final WaterSurfaceInstance surface);

    void add(@NonNull final TerrainSurfaceInstance surface);

    /** Removes the instance from the scene. The instance itself is not deleted */
    void remove(@NonNull final AbstractInstance instance);
}
