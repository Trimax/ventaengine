package com.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GizmoType {
    Origin("origin"),
    Object("box"),
    Camera("frustum"),
    Light("gizmo");

    private final String value;
}
