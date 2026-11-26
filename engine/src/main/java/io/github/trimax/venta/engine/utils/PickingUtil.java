package io.github.trimax.venta.engine.utils;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.pick.PickResult;
import io.github.trimax.venta.engine.model.instance.SceneInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Finds the thing of a scene under a point of the screen: the ray of the point meets the boxes of the objects and the marks of the
 * things that have no shape, and the nearest is chosen. The ground and the water are not chosen by a click.
 */
@UtilityClass
public final class PickingUtil {
    /**
     * The nearest thing of a scene under a point of the screen.
     *
     * @param ndcX       the point from -1 (left) to 1 (right)
     * @param ndcY       the point from -1 (bottom) to 1 (top)
     * @param view       the view matrix of the camera
     * @param projection the projection matrix
     * @return the nearest thing under the point, null when there is none
     */
    public PickResult pick(@NonNull final SceneInstance scene, final float ndcX, final float ndcY,
                           @NonNull final Matrix4fc view, @NonNull final Matrix4fc projection) {
        final var inverse = new Matrix4f(projection).mul(view).invert();
        final var ray = RayUtil.through(ndcX, ndcY, inverse);
        final var camera = new Matrix4f(view).invert().getTranslation(new Vector3f());

        PickResult nearest = null;

        for (final var object : scene.getObjects()) {
            if (!(object instanceof ObjectInstanceImplementation entity) || !entity.isVisible())
                continue;

            final var distance = RayUtil.intersectBox(ray, entity.getBox(), entity.getTransform().getMatrix());
            if (distance != RayUtil.MISS && (nearest == null || distance < nearest.distance()))
                nearest = new PickResult(object, distance);
        }

        for (final var mark : OverlayUtil.marks(scene)) {
            final var distance = RayUtil.intersectSphere(ray, mark.position(), OverlayUtil.markSize(mark.position(), camera));
            if (distance != RayUtil.MISS && (nearest == null || distance < nearest.distance()))
                nearest = new PickResult(mark.instance(), distance);
        }

        return nearest;
    }
}
