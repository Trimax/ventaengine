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
    Light("bipyramid.json");

    private final String mesh;
}
