package io.github.trimax.venta.editor.listeners.mixer;

import javax.imageio.ImageIO;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.context.MixerContext;
import io.github.trimax.venta.editor.enums.Channel;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.mixer.TexturePickEvent;
import io.github.trimax.venta.editor.model.event.mixer.TextureSelectEvent;
import io.github.trimax.venta.editor.utils.ImageViewUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureSelectListener implements AbstractListener<TextureSelectEvent> {
    private final MixerContext mixerContext;

    @Override
    @Subscribe
    @SneakyThrows
    public void handle(@NonNull final TextureSelectEvent event) {
        final var imageSource = ImageIO.read(event.file());
        mixerContext.setSourceImage(event.textureSlot(), imageSource);
        ImageViewUtil.setImage(imageSource, event.imgTexture());

        EventUtil.post(new TexturePickEvent(event.textureSlot(), Channel.Red, event.imgChannel(), 0));
    }
}
