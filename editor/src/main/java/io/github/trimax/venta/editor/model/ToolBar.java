package io.github.trimax.venta.editor.model;

import javafx.scene.control.Button;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class ToolBar {
    @NonNull
    private final Button btnToolBarArchiveNew;

    @NonNull
    private final Button btnToolBarFileAdd;

    @NonNull
    private Button btnToolBarFileRemove;

    @NonNull
    private Button btnToolBarFolderAdd;

    @NonNull
    private Button btnToolBarFolderRemove;
}
