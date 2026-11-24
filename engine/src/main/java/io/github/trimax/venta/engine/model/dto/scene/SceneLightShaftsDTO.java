package io.github.trimax.venta.engine.model.dto.scene;

/**
 * The light shafts of a scene: the light of the sun that is scattered in the fog and reaches the camera through the gaps
 * between what shadows it (trees, a castle, hills). They need an exponential fog (the fog is what scatters the light) and
 * the shadows of the sun (they tell where the light reaches).
 *
 * @param intensity how bright the shafts are, 0 is none
 * @param steps     how many points along a view ray are tested against the shadows: the more, the smoother the shafts
 * @param anisotropy how much the fog scatters the light forward, towards the camera that looks at the sun: from -1 (back) to 1 (forward)
 */
public record SceneLightShaftsDTO(Float intensity, Integer steps, Float anisotropy) {
}
