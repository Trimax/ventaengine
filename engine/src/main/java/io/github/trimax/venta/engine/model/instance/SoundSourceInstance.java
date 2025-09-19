package io.github.trimax.venta.engine.model.instance;

import lombok.NonNull;
import org.joml.Vector3fc;

public interface SoundSourceInstance extends AbstractInstance {
    float getVolume();

    float getPitch();

    Vector3fc getPosition();

    void setVolume(final float volume);

    void setPitch(final float pitch);

    void setPosition(@NonNull final Vector3fc position);

    boolean isLooping();

    void setLooping(final boolean value);

    void play();

    void stop();

    boolean isPlaying();
}
