package com.venta.engine.core;

import com.venta.engine.annotations.Component;
import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.manager.ObjectManager;
import com.venta.engine.manager.ProgramManager;
import com.venta.engine.manager.SceneManager;
import com.venta.engine.manager.WindowManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

@Slf4j
@Component
@RequiredArgsConstructor
public final class Engine implements Runnable {
    private final FPSCounter fpsCounter;
    private final Context context;

    @Setter
    private Venta venta;

    @Setter
    private WindowManager.WindowEntity window;

    public void initialize(final WindowConfiguration windowConfiguration) {
        position = new float[]{0.f, 0.f, 0f};
        rotation = new float[]{0.f, 0.f, 0f};
        scale    = new float[]{1.f, 1.f, 1f};

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = context.getWindowManager()
                .create(windowConfiguration.title(), windowConfiguration.width(), windowConfiguration.height());

        GL.createCapabilities();
    }

    private ProgramManager.ProgramEntity shaderProgram;

    private float[] position;
    private float[] rotation;
    private float[] scale;

    @Override
    public void run() {
        glEnable(GL_DEPTH_TEST);

        shaderProgram = createShader();

        loop();

        //TODO: Deinitialize managers

        glfwTerminate();
    }

    private void loop() {
        glfwMakeContextCurrent(window.getId());
        glfwSwapInterval(1); // vertical synchronization (setting to 0 produces 5000 FPS)

        final int positionLocation = glGetUniformLocation(shaderProgram.getIdAsInteger(), "translation");
        final int rotationLocation = glGetUniformLocation(shaderProgram.getIdAsInteger(), "rotation");
        final int scaleLocation    = glGetUniformLocation(shaderProgram.getIdAsInteger(), "scale");

        while (!glfwWindowShouldClose(window.getId())) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //TODO: Move to onUpdate
            glUseProgram(shaderProgram.getIdAsInteger());

            glUniform3f(positionLocation, position[0], position[1], position[2]);
            glUniform3f(rotationLocation, rotation[0], rotation[1], rotation[2]);
            glUniform3f(scaleLocation,    scale[0],    scale[1],    scale[2]);

            render(context.getSceneManager().getCurrent());

            glfwSwapBuffers(window.getId());
            glfwPollEvents();

            venta.onUpdate(fpsCounter.getDelta(), context);

            rotation[0] += 0.01f;
            rotation[1] += 0.02f;
            rotation[2] += 0.03f;


            fpsCounter.count(window);
        }
    }

    private void render(final SceneManager.SceneEntity scene) {
        if (scene == null)
            return;

        scene.getObjects().forEach(this::render);
    }

    private void render(final ObjectManager.ObjectEntity object) {

        //TODO: Save programID and its arguments in object
        //      bind position, rotation & scale to them

//        glUniform3f(positionLocation, position[0], position[1], position[2]);
//        glUniform3f(rotationLocation, rotation[0], rotation[1], rotation[2]);
//        glUniform3f(scaleLocation,    scale[0],    scale[1],    scale[2]);

        glBindVertexArray(object.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.getBakedObject().facets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private ProgramManager.ProgramEntity createShader() {
        return context.getProgramManager().link("Basic",
                context.getShaderManager().load("basic_vertex"),
                context.getShaderManager().load("basic_fragment"));
    }
}

