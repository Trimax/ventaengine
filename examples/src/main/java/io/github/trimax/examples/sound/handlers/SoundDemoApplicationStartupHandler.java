package io.github.trimax.examples.sound.handlers;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.interfaces.VentaEngineStartupHandler;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
public final class SoundDemoApplicationStartupHandler implements VentaEngineStartupHandler {
    @Override
    public void onStartup(final String[] args, final VentaContext context) {
        final var scene = context.getSceneManager().getCurrent();
        scene.setAmbientLight(new Vector3f(0.8f, 0.8f, 0.8f));

        final var camera = context.getCameraManager().getCurrent();
        camera.setPosition(new Vector3f(5.f, 5.f, 5.f));
        camera.lookAt(new Vector3f(0.f));

        final var sound1 = context.getSoundSourceManager().create("engine1", context.getSoundSourceRepository().get("default.json"));
        sound1.setPosition(new Vector3f(1.0f, 0.0f, 0.0f));  
        sound1.setVolume(1.0f);
        sound1.setPitch(1.0f);                            
        sound1.play();
        scene.add(sound1);

        final var sound2 = context.getSoundSourceManager().create("engine2", context.getSoundSourceRepository().get("default.json"));
        sound2.setPosition(new Vector3f(-1.0f, 0.0f, 0.0f)); 
        sound2.setVolume(1.0f);
        sound2.setPitch(0.5f);                               
        sound2.play();
        scene.add(sound2);
        
    }
}
