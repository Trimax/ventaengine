package io.github.trimax.venta.editor.listeners.mixer;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.MixerContext;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.mixer.TextureMixEvent;
import io.github.trimax.venta.editor.model.event.mixer.TexturePickEvent;
import io.github.trimax.venta.editor.utils.ImageUtil;
import io.github.trimax.venta.editor.utils.ImageViewUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TexturePickListener implements AbstractListener<TexturePickEvent> {
    private final MixerContext mixerContext;

    @Override
    @Subscribe
    public void handle(@NonNull final TexturePickEvent event) {
        final var imageChannel = ImageUtil.extract(mixerContext.getSourceImage(event.textureSlot()), event.channel(), event.value());
        mixerContext.setChannelImage(event.textureSlot(), imageChannel);
        ImageViewUtil.setImage(imageChannel, event.imgChannel());

        EventUtil.post(new TextureMixEvent());
    }
}
