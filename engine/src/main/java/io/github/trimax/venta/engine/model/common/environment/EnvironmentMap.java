package io.github.trimax.venta.engine.model.common.environment;

/**
 * The light of the environment convolved from a skybox for the image based lighting.
 *
 * @param irradianceID    the cube map of what a surface diffuses from every direction
 * @param prefilterID     the cube map of what a surface reflects: the sky blurred by the roughness, a mip level for every roughness
 * @param prefilterLevels the number of the mip levels of the prefiltered map
 * @param brdfID          the lookup table of the BRDF by the angle and the roughness
 */
public record EnvironmentMap(int irradianceID, int prefilterID, int prefilterLevels, int brdfID) {
}
