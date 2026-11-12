package io.github.trimax.venta.engine.model.prefabs.implementation;

import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.shared.Attenuation;

/** Creates prefabs without resources for tests */
public final class TestPrefabs {
    private TestPrefabs() {
    }

    public static LightPrefabImplementation light() {
        return new LightPrefabImplementation(new Vector3f(1), Attenuation.createDefault(), false, 1.f, 10.f);
    }
}
