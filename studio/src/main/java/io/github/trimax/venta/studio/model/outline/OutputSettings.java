package io.github.trimax.venta.studio.model.outline;

import io.github.trimax.venta.engine.enums.ToneMapping;

/**
 * How the image of a scene is finished: the light is multiplied by the exposure and mapped to the colors of the screen.
 */
public record OutputSettings(Float exposure, ToneMapping toneMapping) {
}
