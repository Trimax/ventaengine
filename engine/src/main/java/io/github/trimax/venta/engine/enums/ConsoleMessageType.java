package io.github.trimax.venta.engine.enums;

import org.joml.Vector3f;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConsoleMessageType {
    Error(new Vector3f(0.8f, 0.2f, 0.2f)),
    Warning(new Vector3f(0.8f, 0.6f, 0.2f)),
    Info(new Vector3f(0.2f, 0.8f, 0.2f)),
    Command(new Vector3f(1.f, 1.f, 1.f)),;

    private final Vector3f color;
}
