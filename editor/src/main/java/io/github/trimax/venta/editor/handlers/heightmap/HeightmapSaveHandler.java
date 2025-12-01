package io.github.trimax.venta.editor.handlers.heightmap;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.context.HeightmapContext;
import io.github.trimax.venta.editor.utils.DialogUtil;
import io.github.trimax.venta.editor.utils.ImageUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapSaveHandler implements EventHandler<ActionEvent> {
    private final HeightmapContext context;

    @Override
    public void handle(final @NonNull ActionEvent event) {
        final var filters = Map.of("PNG Image (*.png)", List.of("*.png"));
        DialogUtil.showFileSave("Save Heightmap", this::save, filters);
    }

    private void save(final @NonNull File file) {
        ImageUtil.write(file, context.getHeightmap());
    }
}
