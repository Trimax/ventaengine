package com.venta.examples.cube;

import org.joml.Vector3f;

import com.venta.engine.configurations.WindowConfiguration;
import com.venta.engine.core.Context;
import com.venta.engine.interfaces.Venta;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.ProgramView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RotatingCube implements Venta {
    private final Vector3f angles = new Vector3f(0.01f, 0.02f, 0.03f);
    private ObjectView cube;

    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube", 1024, 768, false);
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        cube = context.getObjectManager().load("cube.json");

        final var program = createShader(context);
        cube.setProgram(program);

        final var scene = context.getSceneManager().create("Sample scene");
        context.getSceneManager().setCurrent(scene);
        scene.getObjects().add(cube);
    }

    @Override
    public void onUpdate(final double delta, final Context context) {
        cube.rotate(angles);
    }

    private ProgramView createShader(final Context context) {
        return context.getProgramManager().load("basic");
    }
}
