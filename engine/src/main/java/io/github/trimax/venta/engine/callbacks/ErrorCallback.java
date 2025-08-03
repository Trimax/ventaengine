package io.github.trimax.venta.engine.callbacks;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;

@Slf4j
@NoArgsConstructor
public final class ErrorCallback extends GLFWErrorCallback implements AbstractCallback {
    @Override
    public void invoke(final int error, final long description) {
        log.error("GLFW Error [{}]: {}", error, GLFWErrorCallback.getDescription(description));
    }
}
