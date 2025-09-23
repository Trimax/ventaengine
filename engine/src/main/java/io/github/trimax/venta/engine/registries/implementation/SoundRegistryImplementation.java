package io.github.trimax.venta.engine.registries.implementation;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.definitions.DefinitionsSound;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.entity.SoundEntity;
import io.github.trimax.venta.engine.model.entity.implementation.Abettor;
import io.github.trimax.venta.engine.model.entity.implementation.SoundEntityImplementation;
import io.github.trimax.venta.engine.registries.SoundRegistry;
import io.github.trimax.venta.engine.services.ResourceService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SoundRegistryImplementation
        extends AbstractRegistryImplementation<SoundEntityImplementation, SoundEntity, Void>
        implements SoundRegistry {
    private final ResourceService resourceService;
    private final Abettor abettor;
    private final Memory memory; 

    @Override
    protected SoundEntityImplementation load(@NonNull final String resourcePath, final Void argument) {
        return load(resourcePath, resourceService.getAsBytes(String.format("/sounds/%s", resourcePath)));
    }

    private SoundEntityImplementation load(final String name, final byte[] data) {
        try (final var info = STBVorbisInfo.malloc()) {
            final var buffer = readVorbis(data, info);
            final var duration = getDuration(buffer, info);

            final var id = memory.getAudioBuffers().create("Sound %s", name);

            alBufferData(id, getFormat(info), buffer, DefinitionsSound.FREQUENCY);
            MemoryUtil.memFree(buffer);

            return abettor.createSound(id, duration);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load sound: " + e);
        }
    }

    private int getFormat(final STBVorbisInfo info) {
        return info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
    }

    private static float getDuration(final ShortBuffer buffer, final STBVorbisInfo info) {
        return (float) (buffer.limit() / info.channels()) / info.sample_rate();
    }

    private ShortBuffer readVorbis(@NonNull final byte[] data, @NonNull final STBVorbisInfo info) {
        try (final var stack = MemoryStack.stackPush()) {
            final var audioBuffer = MemoryUtil.memAlloc(data.length);
            audioBuffer.put(data).flip();

            return getBuffer(info, stack, audioBuffer);
        }
    }

    private ShortBuffer getBuffer(final STBVorbisInfo info, final MemoryStack stack, final ByteBuffer audioBuffer) {
        try {
            final var error = stack.mallocInt(1);
            final var decoder = stb_vorbis_open_memory(audioBuffer, error, null);

            if (decoder == NULL)
                throw new RuntimeException("Failed to open Ogg Vorbis data. Error: " + error.get(0));

            return getBuffer(info, decoder);
        } finally {
            MemoryUtil.memFree(audioBuffer);
        }
    }

    private ShortBuffer getBuffer(final STBVorbisInfo info, final long decoder) {
        try {
            stb_vorbis_get_info(decoder, info);

            final var channels = info.channels();
            final var lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

            final var buffer = MemoryUtil.memAllocShort(lengthSamples * channels);

            final var samplesRead = stb_vorbis_get_samples_short_interleaved(decoder, channels, buffer);
            buffer.limit(samplesRead * channels);

            return buffer;
        } finally {
            stb_vorbis_close(decoder);
        }
    }

    @Override
    protected void unload(@NonNull final SoundEntityImplementation entity) {
        log.info("Unloading sound {}", entity.getID());

        memory.getAudioBuffers().delete(entity.getBufferID());
    }
}