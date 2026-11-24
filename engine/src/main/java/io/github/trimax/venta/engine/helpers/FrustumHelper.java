package io.github.trimax.venta.engine.helpers;

import org.joml.FrustumIntersection;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import lombok.NonNull;

/**
 * Tells what is out of a view: the bounding box of an object in its own space and the matrix of the object give its box in
 * the world, which is tested against the planes of a view projection matrix (the camera, or the sun for a shadow map).
 *
 * <p>It is asked for every object of every frame, so one instance keeps its scratch vectors: a test allocates nothing.
 */
public final class FrustumHelper {
    /** Every plane of the view: the sides, the near plane and the far plane */
    public static final int ALL_PLANES = FrustumIntersection.PLANE_MASK_NX | FrustumIntersection.PLANE_MASK_PX
            | FrustumIntersection.PLANE_MASK_NY | FrustumIntersection.PLANE_MASK_PY
            | FrustumIntersection.PLANE_MASK_NZ | FrustumIntersection.PLANE_MASK_PZ;

    /**
     * The sides of the view only. What is nearer to the sun than the shadow map still casts its shadow into it (the
     * shadow pass clamps the depth), and what is farther can't shadow anything the map holds: only the sides decide.
     */
    public static final int SIDE_PLANES = FrustumIntersection.PLANE_MASK_NX | FrustumIntersection.PLANE_MASK_PX
            | FrustumIntersection.PLANE_MASK_NY | FrustumIntersection.PLANE_MASK_PY;

    private final FrustumIntersection frustum = new FrustumIntersection();
    private final Vector3f minimum = new Vector3f();
    private final Vector3f maximum = new Vector3f();

    private int planes = ALL_PLANES;

    /**
     * Sets the view for the next tests.
     *
     * @param viewProjection the matrix from the world to the clip space of the view
     * @param planes         the planes of the view that cull: {@link #ALL_PLANES} or {@link #SIDE_PLANES}
     */
    public void set(@NonNull final Matrix4fc viewProjection, final int planes) {
        this.frustum.set(viewProjection, false);
        this.planes = planes;
    }

    /**
     * Whether a box is at least partly in the view. A box without a point (a model without meshes) is never culled: it
     * is not known where it is.
     *
     * @param box    the box in the space of the object
     * @param model  the matrix of the object, it may turn and scale the box
     * @param margin how far the object may reach out of its box in the world, like a tree swaying in the wind
     */
    public boolean isVisible(@NonNull final BoundingBox box, @NonNull final Matrix4fc model, final float margin) {
        final var from = box.min();
        final var to = box.max();
        if (!(from.x <= to.x && from.y <= to.y && from.z <= to.z) || !Float.isFinite(from.x + from.y + from.z + to.x + to.y + to.z))
            return true;

        model.transformAab(from, to, minimum, maximum);

        return frustum.intersectAab(minimum.x - margin, minimum.y - margin, minimum.z - margin,
                maximum.x + margin, maximum.y + margin, maximum.z + margin, planes) < 0;
    }
}
