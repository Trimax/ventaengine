package io.github.trimax.venta.engine.model.dto.scene;

import io.github.trimax.venta.engine.model.common.shared.Range;
import io.github.trimax.venta.engine.model.dto.common.ColorDTO;
import lombok.NonNull;

/**
 * The fog of a scene. It has two kinds: a layer (the distances between which the fog grows, and the height of its top)
 * and an exponential fog (its density falls with the height, so it lies in the valleys and thins out on the hills). The
 * exponential one is chosen by the density.
 *
 * @param distance      where the fog of a layer starts and where it is whole; not needed for the exponential fog
 * @param color         the color of the fog
 * @param height        the top of the layer of a fog of a layer
 * @param density       the density of the exponential fog at its base height: the part of the light that is lost
 *                      on a unit of the way is about this much, the fog of 1 / density units is thick
 * @param falloff       how fast the exponential fog thins with the height: the density falls e times on 1 / falloff units
 * @param baseHeight    the height where the density of the exponential fog is the given one
 * @param sunScattering how much the fog takes the color of the sun when it is seen towards the sun, from 0 to 1
 * @param sunSharpness  how narrow the glow around the sun is: the greater, the narrower
 */
public record SceneFogDTO(Range<Float> distance,
                          @NonNull ColorDTO color,
                          Float height,
                          Float density,
                          Float falloff,
                          Float baseHeight,
                          Float sunScattering,
                          Float sunSharpness) {
}