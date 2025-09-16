package io.github.trimax.venta.engine.model.instance.implementation;

import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.model.instance.SoundSourceInstance;
import io.github.trimax.venta.engine.model.prefabs.implementation.SoundSourcePrefabImplementation;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

@Slf4j
@Getter
public final class SoundSourceInstanceImplementation extends AbstractInstanceImplementation implements SoundSourceInstance {
    private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
    private final SoundEntityImplementation soundEntity;
    private final int sourceId;
    private final int bufferId;
    private float volume;
    private float pitch;
    private boolean looping;

    SoundSourceInstanceImplementation(@NonNull final String name,
                                      @NonNull final SoundSourcePrefabImplementation prefab) {
        super(null, name);

        this.soundEntity = prefab.getSound();
        this.volume = prefab.getVolume();
        this.pitch = prefab.getPitch();
        this.looping = prefab.isLooping();

        this.sourceId = alGenSources();
        this.bufferId = alGenBuffers();

        setupAudioBuffer();
        setupAudioSource();
    }

    private void setupAudioBuffer() {
        final ShortBuffer buffer = soundEntity.getBuffer();
        final int format = (buffer.limit() % 2 == 0) ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;

        alBufferData(bufferId, format, buffer, 44100);

        final int error = alGetError();
        if (error != AL_NO_ERROR)
            log.error("OpenAL error while setting up buffer: {}", error);
    }

    private void setupAudioSource() {
        alSourcei(sourceId, AL_BUFFER, bufferId);
        alSourcef(sourceId, AL_GAIN, volume);
        alSourcef(sourceId, AL_PITCH, pitch);
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
        alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);

        final int error = alGetError();
        if (error != AL_NO_ERROR)
            log.error("OpenAL error while setting up source: {}", error);
    }


    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public void setVolume(final float volume) {
        this.volume = Math.max(0.0f, volume);
        alSourcef(sourceId, AL_GAIN, this.volume);
    }

    @Override
    public void setPitch(final float pitch) {
        this.pitch = Math.max(0.1f, pitch);
        alSourcef(sourceId, AL_PITCH, this.pitch);
    }

    @Override
    public void setPosition(@NonNull final Vector3fc position) {
        this.position.set(position);
        alSource3f(sourceId, AL_POSITION, position.x(), position.y(), position.z());
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public void setLooping(final boolean value) {
        this.looping = value;
        alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }

    @Override
    public void play() {
        alSourcePlay(sourceId);
    }

    @Override
    public void stop() {
        alSourceStop(sourceId);
    }
}
