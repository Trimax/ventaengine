package io.github.trimax.venta.engine.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector3f;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ConsoleMessageType {
    Debug(new Vector3f(0.6f, 0.6f, 0.6f)),
    Error(new Vector3f(0.8f, 0.2f, 0.2f)),
    Warning(new Vector3f(0.8f, 0.6f, 0.2f)),
    Info(new Vector3f(0.2f, 0.8f, 0.2f)),
    Command(new Vector3f(1.f, 1.f, 1.f)),
    Header(new Vector3f(0.4f, 0.6f, 1.0f));

    private final Vector3f color;
}
