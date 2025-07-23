package com.venta.examples.cube;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;

import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.core.Context;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.interfaces.VentaInputHandler;
import com.venta.engine.model.view.LightView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.ProgramView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RotatingCube implements Venta {
    private final Vector3f angles = new Vector3f(0.f, 0.f, 0.f);
    private final InputHandler inputHandler = new InputHandler();

    private ObjectView cube;
    private LightView light;

    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube", 1024, 768, false);
    }

    @Override
    public VentaInputHandler createInputHandler() {
        return inputHandler;
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        cube = context.getObjectManager().load("cube.json");
        cube.setMaterial(context.getMaterialManager().load("fabric.json"));

        final var program = createShader(context);
        cube.setProgram(program);

        light = context.getLightManager().load("basic.json");
        light.setPosition(new Vector3f(2.f, 2.f, 2.f));

        final var scene = context.getSceneManager().create("Sample scene");
        context.getSceneManager().setCurrent(scene);
        scene.add(light);
        scene.add(cube);

        final var origin = context.getObjectManager().load("origin.json");
        origin.setProgram(program);
        origin.setDrawMode(DrawMode.Edge);
        scene.add(origin);

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(-1.f, 2.f, 3.f));
        camera.lookAt(new Vector3f(0.f));
    }

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
            angles.x = +0.05f;

        cube.rotate(angles);
    }

    private ProgramView createShader(final Context context) {
        return context.getProgramManager().load("basic");
    }
}
