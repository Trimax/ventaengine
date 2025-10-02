package io.github.trimax.venta.editor.listeners.mixer;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.controllers.mixer.MixerController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.mixer.TextureMixEvent;
import io.github.trimax.venta.editor.utils.ImageUtil;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureMixListener implements AbstractListener<TextureMixEvent> {
    private final MixerController mixerController;

    @Override
    @Subscribe
    public void handle(@NonNull final TextureMixEvent event) {
        if (!hasAllChannels())
            return;

        ImageUtil.mix(mixerController.getRedChannel(),
                mixerController.getGreenChannel(),
                mixerController.getBlueChannel(),
                mixerController.getAlphaChannel(),
                mixerController.getImgMix());
    }

    private boolean hasAllChannels() {
        return hasImage(mixerController.getRedChannel()) && hasImage(mixerController.getGreenChannel())
                && hasImage(mixerController.getBlueChannel()) && hasImage(mixerController.getAlphaChannel());
    }

    private boolean hasImage(@NonNull final ImageView view) {
        return view.getImage() != null;
    }
}
