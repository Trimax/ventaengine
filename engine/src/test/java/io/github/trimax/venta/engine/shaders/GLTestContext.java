package io.github.trimax.venta.engine.shaders;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Assumptions;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

/**
 * An OpenGL 3.3 core context for the tests, created once in a hidden window of the thread of the tests.
 * <p>
 * The tests that need it are skipped when the context can't be created: there is no display (the build of a server
 * without Xvfb) or the tests aren't run on the first thread on macOS (see the macOS profiles of the root pom).
 * When the environment variable VENTA_REQUIRE_OPENGL is true (the build on GitHub sets it), there must be a context:
 * the tests fail instead of being skipped, so a broken environment doesn't hide the tests.
 */
final class GLTestContext {
    private static final String REQUIRED = "VENTA_REQUIRE_OPENGL";
    private static final int WINDOW_SIZE = 64;

    private static Boolean created;
    private static String problem = "";

    private GLTestContext() {
    }

    /** Skips the test when there is no context */
    static synchronized void require() {
        if (created == null)
            created = create();

        if (!created && Boolean.parseBoolean(System.getenv(REQUIRED)))
            fail("There is no OpenGL context, but " + REQUIRED + " is set: " + problem);

        Assumptions.assumeTrue(created, () -> "There is no OpenGL context: " + problem);
    }

    private static boolean create() {
        final var errors = new StringBuilder();
        glfwSetErrorCallback(GLFWErrorCallback.create((code, description) ->
                errors.append(GLFWErrorCallback.getDescription(description)).append("; ")));

        try {
            if (!glfwInit()) {
                problem = "GLFW can't be initialized: " + errors;
                return false;
            }
        } catch (final IllegalStateException exception) {
            // LWJGL refuses to use GLFW on macOS outside of the first thread
            problem = exception.getMessage();
            return false;
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        final var window = glfwCreateWindow(WINDOW_SIZE, WINDOW_SIZE, "Venta test", NULL, NULL);
        if (window == NULL) {
            problem = "The window can't be created: " + errors;
            return false;
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        return true;
    }
}
