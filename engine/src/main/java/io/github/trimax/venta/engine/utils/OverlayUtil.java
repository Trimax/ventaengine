package io.github.trimax.venta.engine.utils;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.engine.model.common.overlay.Mark;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * What an editor puts over a scene: which things get marks, and how big a mark is.
 */
@UtilityClass
public final class OverlayUtil {
    /** A mark is at least this big (in meters) and grows with the distance to stay the same size on the screen */
    public static final float MINIMAL_MARK_SIZE = 0.2f;
    private static final float MARK_SIZE_PER_DISTANCE = 0.025f;

    /** The marks of the lights, the emitters, the billboards and the sound sources of a scene */
    public List<Mark> marks(@NonNull final SceneInstance scene) {
        final var marks = new ArrayList<Mark>();

        scene.getLights().forEach(light -> marks.add(new Mark(light, Mark.Kind.Light, light.getPosition())));
        scene.getEmitters().forEach(emitter -> marks.add(new Mark(emitter, Mark.Kind.Emitter, emitter.getPosition())));
        scene.getBillboards().forEach(billboard -> marks.add(new Mark(billboard, Mark.Kind.Billboard, billboard.getPosition())));
        scene.getSoundSources().forEach(sound -> marks.add(new Mark(sound, Mark.Kind.Sound, sound.getPosition())));

        return marks;
    }

    /** The half of the size of a mark that is this far from the camera */
    public float markSize(final float distance) {
        return Math.max(MINIMAL_MARK_SIZE, distance * MARK_SIZE_PER_DISTANCE);
    }

    /** The half of the size of a mark at a place, seen from a place */
    public float markSize(@NonNull final Vector3fc position, @NonNull final Vector3fc camera) {
        return markSize(new Vector3f(position).distance(camera));
    }
}
