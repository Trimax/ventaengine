package com.venta.examples.cube;

import com.venta.engine.core.Context;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.interfaces.VentaEngineApplication;
import com.venta.engine.interfaces.VentaEngineConfiguration;
import com.venta.engine.interfaces.VentaEngineInputHandler;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
public final class RotatingCube implements VentaEngineApplication {
    private final Vector3f angles = new Vector3f(0.f, 0.f, 0.f);
    private final InputHandler inputHandler = new InputHandler();
    private final Random random = new Random();

    private ObjectView gizmo;
    private ObjectView cube;
    private LightView light;

    @Override
    public VentaEngineConfiguration getConfiguration() {
        return new VentaEngineConfiguration() {
            @Override
            public RenderConfiguration getRenderConfiguration() {
                return new RenderConfiguration(false, true);
            }
        };
    }

    @Override
    public VentaEngineInputHandler getInputHandler() {
        return inputHandler;
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        final var scene = context.getSceneManager().getCurrent();

        cube = context.getObjectManager().load("cube");
        cube.getMesh().setMaterial(context.getMaterialManager().load("stone"));
        cube.setScale(new Vector3f(2.f));
        scene.add(cube);

        light = context.getLightManager().load("basic");
        light.setPosition(new Vector3f(2.f, 2.f, 2.f));
        scene.add(light);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        gizmo = context.getObjectManager().load("gizmo");
        gizmo.setDrawMode(DrawMode.Edge);
        scene.add(gizmo);

        final int cubeCount = 10;
        for (int cubeID = 0; cubeID < cubeCount; cubeID++) {
            final var miniCube = context.getObjectManager().create("cube" + cubeID, cube.getMesh(), cube.getProgram());
            miniCube.setPosition(new Vector3f(
                    3.f * (float) Math.sin(cubeID * (2 * Math.PI / cubeCount)),
                    0.f,
                    3.f * (float) Math.cos(cubeID * (2 * Math.PI / cubeCount))));
            scene.add(miniCube);
        }
    }

    private double elapsedTime = 0.0;

    @Override
    public void onUpdate(final double delta, final Context context) {
        angles.x = 0.f;
        angles.y = 0.f;

        if (inputHandler.isButtonPushed(GLFW_KEY_LEFT))
            angles.y = -0.05f;

        if (inputHandler.isButtonPushed(GLFW_KEY_RIGHT))
            angles.y = 0.05f;

        if (inputHandler.isButtonPushed(GLFW_KEY_UP))
            angles.x = -0.05f;

        if (inputHandler.isButtonPushed(GLFW_KEY_DOWN))
            angles.x = 0.05f;

        cube.rotate(angles);

        elapsedTime += delta;
        light.setPosition(new Vector3f(
                2.5f * (float) Math.sin(elapsedTime),
                2.5f,
                2.5f * (float) Math.cos(elapsedTime)
        ));

        if (inputHandler.isButtonPushed(GLFW_KEY_SPACE))
            light.setColor(createRandomVector3());

        gizmo.setPosition(light.getPosition());
    }

    public Vector3f createRandomVector3() {
        return new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }
}
