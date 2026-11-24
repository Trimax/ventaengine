package io.github.trimax.venta.engine.model.dto.scene;

/**
 * The bloom of a scene: the glow around what is brighter than the threshold.
 *
 * @param intensity how much of the glow is added to the image (a share of the light of the scene), 0 is no bloom
 * @param threshold the brightness (in the linear HDR light before the exposure) from which a pixel glows
 * @param softKnee  how softly the glow starts around the threshold, from 0 (a hard edge) to 1
 */
public record SceneBloomDTO(Float intensity, Float threshold, Float softKnee) {
}
