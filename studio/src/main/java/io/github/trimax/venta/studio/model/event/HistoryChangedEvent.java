package io.github.trimax.venta.studio.model.event;

/**
 * What can be undone or redone has changed, or whether the scene is as it was saved.
 *
 * @param undoLabel what undo does, an empty text for nothing
 * @param redoLabel what redo does, an empty text for nothing
 * @param dirty     whether the scene has changes that are not saved
 */
public record HistoryChangedEvent(String undoLabel, String redoLabel, boolean dirty) {
}
