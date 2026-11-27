package io.github.trimax.venta.studio.services;

import com.google.common.eventbus.Subscribe;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.studio.StudioSession;
import io.github.trimax.venta.studio.model.event.HistoryChangedEvent;
import io.github.trimax.venta.studio.model.event.SceneOpenedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The title of the window: the scene that is open, and a star when it has changes that are not saved.
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowTitleService {
    private static final String NAME = "Venta Studio";

    private final StudioSession session;

    @Subscribe
    public void onSceneOpened(final SceneOpenedEvent event) {
        show(event.name(), false);
    }

    @Subscribe
    public void onHistoryChanged(final HistoryChangedEvent event) {
        if (session.getScene() != null)
            show(session.getScene(), event.dirty());
    }

    private void show(final String scene, final boolean dirty) {
        if (session.getStage() != null)
            session.getStage().setTitle(NAME + " - " + scene + (dirty ? " *" : ""));
    }
}
