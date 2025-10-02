package io.github.trimax.venta.editor.listeners.mixer;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.enums.Channel;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.mixer.TextureMixEvent;
import io.github.trimax.venta.editor.model.event.mixer.TextureSelectEvent;
import io.github.trimax.venta.editor.utils.ImageUtil;
import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextureSelectListener implements AbstractListener<TextureSelectEvent> {
    @Override
    @Subscribe
    public void handle(@NonNull final TextureSelectEvent event) {
        event.imgTexture().setImage(new Image(event.file().toURI().toString()));
        ImageUtil.copyChannel(event.imgTexture(), event.imgChannel(), Channel.Red, 0);
        EventUtil.post(new TextureMixEvent());
    }
}
