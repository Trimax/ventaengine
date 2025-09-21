package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.lwjgl.openal.AL10.*;

@Slf4j
@Getter
public final class SoundSourceInstanceImplementation extends AbstractInstanceImplementation implements SoundSourceInstance {
    private final Vector3f position = new Vector3f(0.f);
    private final SoundEntityImplementation soundEntity;
    private final int sourceID;
    private float volume;
    private float pitch;
    private boolean looping;

    SoundSourceInstanceImplementation(@NonNull final String name,
                                      @NonNull final SoundEntityImplementation sound,
                                      final float volume,
                                      final float pitch,
                                      final boolean looping,
                                      final int sourceID,
                                      @NonNull final GizmoInstanceImplementation gizmo) {
        super(gizmo, name);

        this.soundEntity = sound;
        this.sourceID = sourceID;
        this.looping = looping;
        this.volume = volume;
        this.pitch = pitch;

        setupAudioSource();
    }

    private void setupAudioSource() {
        alSourcei(sourceID, AL_BUFFER, soundEntity.getBufferID());
        alSourcef(sourceID, AL_GAIN, volume);
        alSourcef(sourceID, AL_PITCH, pitch);
        alSource3f(sourceID, AL_POSITION, position.x, position.y, position.z);
        alSourcei(sourceID, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }

    @Override
    public void setVolume(final float volume) {
        this.volume = Math.clamp(volume, 0.f, 1.f);
        alSourcef(sourceID, AL_GAIN, this.volume);
    }

    @Override
    public void setPitch(final float pitch) {
        this.pitch = Math.max(0.f, pitch);
        alSourcef(sourceID, AL_PITCH, this.pitch);
    }

    @Override
    public void setPosition(@NonNull final Vector3fc position) {
        this.position.set(position);
        alSource3f(sourceID, AL_POSITION, position.x(), position.y(), position.z());
    }

    @Override
    public void setLooping(final boolean value) {
        this.looping = value;
        alSourcei(sourceID, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }

    @Override
    public void play() {
        alSourcePlay(sourceID);
    }

    @Override
    public void stop() {
        alSourceStop(sourceID);
    }

    @Override
    public boolean isPlaying() {
        return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PLAYING;
    }
}
