package io.github.trimax.venta.editor.handlers.resource;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.EventUtil;
import io.github.trimax.venta.editor.model.event.resource.ResourceAddEvent;
import io.github.trimax.venta.editor.utils.DialogUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceAddHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(final ActionEvent event) {
        DialogUtil.showFileOpen("Please choose a resource to add", this::resourceAdd,
                Map.of("All Files (*.*)", List.of("*.*")));
    }

    private void resourceAdd(final File file) {
        EventUtil.post(new ResourceAddEvent(file));
    }
}
