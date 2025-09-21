package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GizmoType {
    Origin("origin.json"),
    Object("box.json"),
    Camera("frustum.json"),
    Emitter("bipyramid.json"),
    Light("light.json"),
    SoundSource("light.json");

    private final String mesh;
}
