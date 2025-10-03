package io.github.trimax.venta.editor.context;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.enums.TextureSlot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import one.util.streamex.StreamEx;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MixerContext {
    private final Map<TextureSlot, BufferedImage> images = new ConcurrentHashMap<>();
    private final Map<TextureSlot, BufferedImage> channels = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private BufferedImage mixedImage;
    
    public void setSourceImage(@NonNull final TextureSlot channel, @NonNull final BufferedImage image) {
        images.put(channel, image);
    }

    public BufferedImage getSourceImage(@NonNull final TextureSlot channel) {
        return images.get(channel);
    }

    public void setChannelImage(@NonNull final TextureSlot channel, @NonNull final BufferedImage image) {
        channels.put(channel, image);
    }

    public BufferedImage getChannelImage(@NonNull final TextureSlot channel) {
        return channels.get(channel);
    }

    public boolean hasAllChannels() {
        return StreamEx.of(TextureSlot.values()).allMatch(channels::containsKey);
    }
}
