package io.github.trimax.venta.editor.listeners.mixer;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.context.MixerContext;
import io.github.trimax.venta.editor.controllers.mixer.MixerController;
import io.github.trimax.venta.editor.enums.TextureSlot;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.mixer.TextureMixEvent;
import io.github.trimax.venta.editor.utils.ImageUtil;
import io.github.trimax.venta.editor.utils.ImageViewUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureMixListener implements AbstractListener<TextureMixEvent> {
    private final MixerController mixerController;
    private final MixerContext mixerContext;

    @Override
    @Subscribe
    public void handle(@NonNull final TextureMixEvent event) {
        if (!mixerContext.hasAllChannels())
            return;

        final var mixedImage = ImageUtil.mix(mixerContext.getChannelImage(TextureSlot.Red),
                mixerContext.getChannelImage(TextureSlot.Green),
                mixerContext.getChannelImage(TextureSlot.Blue),
                mixerContext.getChannelImage(TextureSlot.Alpha));

        mixerContext.setMixedImage(mixedImage);
        ImageViewUtil.setImage(mixedImage, mixerController.getImgMix());
    }
}
