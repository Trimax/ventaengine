package com.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DebugVisualType {
    Origin("simple"),
    Gizmo("simple");

    private final String program;
}
