package io.github.trimax.venta.editor.model.common;

import io.github.trimax.venta.editor.enums.Channel;
import javafx.scene.paint.Color;
import lombok.NonNull;

public record RawColor(int r, int g, int b, int a) {
    public Color toColor() {
        return Color.rgb(r, g, b, (double) a / 255.0);
    }

    public int getChannel(@NonNull final Channel channel, final boolean hasAlpha, final int value) {
        return switch (channel) {
            case Red -> r;
            case Green -> g;
            case Blue -> b;
            case Alpha -> hasAlpha ? a : 255;
            case Value -> value;
        };
    }
}
