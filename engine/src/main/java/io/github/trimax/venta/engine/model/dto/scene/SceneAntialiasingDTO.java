package io.github.trimax.venta.engine.model.dto.scene;

import io.github.trimax.venta.engine.enums.AntialiasingType;
import io.github.trimax.venta.engine.enums.FxaaQuality;

/**
 * The antialiasing of a scene: the smoothing of the edges that the multisampling doesn't touch.
 *
 * @param type          the method, FXAA when it is not set
 * @param quality       how far FXAA looks along an edge
 * @param subpixel      how much FXAA smooths the lines that are thinner than a pixel, from 0 (none) to 1
 * @param edgeThreshold the contrast of the brightness from which a pixel is on an edge: the less, the more edges are smoothed
 */
public record SceneAntialiasingDTO(AntialiasingType type, FxaaQuality quality, Float subpixel, Float edgeThreshold) {
}
