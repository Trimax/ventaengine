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

    /** The sound has its full volume up to this distance from the listener, then it fades */
    float getReferenceDistance();

    /** Beyond this distance the sound doesn't fade anymore */
    float getMaximalDistance();

    /** How fast the sound fades with the distance: 0 is no fading, 1 is the physical fading */
    float getRolloff();

    void setReferenceDistance(final float distance);

    void setMaximalDistance(final float distance);

    void setRolloff(final float rolloff);

    boolean isLooping();

    void setLooping(final boolean value);

    void play();

    void stop();

    boolean isPlaying();
}
