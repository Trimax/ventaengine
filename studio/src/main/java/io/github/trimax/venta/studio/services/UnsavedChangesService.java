package io.github.trimax.venta.studio.services;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.StudioSession;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Asks what to do with the changes of the scene that are not saved before something that would lose them: another scene is open, the
 * window is closed.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnsavedChangesService {
    private final StudioSession session;
    private final SceneEditService edits;

    /**
     * Asks what to do with the changes that are not saved, when there are any.
     *
     * @return whether to go on: there is nothing to save, or the scene was saved, or the user doesn't want it saved; false when the
     *         user cancelled (or the scene could not be saved)
     */
    public boolean confirm() {
        if (!edits.isDirty())
            return true;

        final var save = new ButtonType("Save");
        final var discard = new ButtonType("Don't Save");
        final var alert = new Alert(Alert.AlertType.CONFIRMATION, "", save, discard, ButtonType.CANCEL);
        alert.setTitle("Unsaved changes");
        alert.setHeaderText("Save the changes of " + session.getScene() + "?");
        alert.setContentText("The changes are lost if you don't save them.");
        alert.initOwner(session.getStage());

        final var answer = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (answer == save)
            return edits.save();

        return answer == discard;
    }
}
