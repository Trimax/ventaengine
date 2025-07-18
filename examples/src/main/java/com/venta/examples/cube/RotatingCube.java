package com.venta.examples.cube;

import com.venta.engine.configuration.WindowConfiguration;
import com.venta.engine.core.Context;
import com.venta.engine.interfaces.Venta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RotatingCube implements Venta {
    @Override
    public WindowConfiguration createWindowConfiguration() {
        return new WindowConfiguration("Rotating cube", 1024, 768, false);
    }

    @Override
    public void onStartup(final String[] args, final Context context) {
        log.info("Rotating cube started");

        final var scene = context.getSceneManager().create("Sample scene");

        final var myCube = context.getObjectManager().load("cube.json");
        scene.getObjects().add(myCube);
    }

    @Override
    public void onUpdate(final long delta, final Context context) {

    }
}
