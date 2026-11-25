package io.github.trimax.venta.engine.helpers;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import io.github.trimax.venta.engine.utils.ObliqueProjectionUtil;
import lombok.Getter;
import lombok.NonNull;

/**
 * The mirror image of a camera in the plane of a water: what the camera sees in the water is what its mirror image, under the
 * plane, sees looking up through it. The matrices of the mirror image have the plane in place of their near plane, so the
 * geometry under the water is cut away by the hardware.
 *
 * <p>It is asked in every frame, so one instance keeps its objects and a frame allocates nothing for them.
 */
@Getter
public final class WaterReflectionHelper {
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewProjectionMatrix = new Matrix4f();

    /** Where the mirror image of the camera is */
    private final Vector3f position = new Vector3f();

    private final Vector3f target = new Vector3f();
    private final Vector3f up = new Vector3f();
    private final Vector4f plane = new Vector4f();
    private final Matrix4f scratch = new Matrix4f();

    /**
     * Makes the matrices of the mirror image of a camera.
     *
     * @param cameraPosition where the camera is
     * @param front          where it looks
     * @param cameraUp       its up direction
     * @param projection     the projection of the camera
     * @param height         the height of the plane of the water
     */
    public void update(@NonNull final Vector3fc cameraPosition, @NonNull final Vector3fc front, @NonNull final Vector3fc cameraUp,
                       @NonNull final Matrix4fc projection, final float height) {
        // A point is mirrored in the plane by negating its distance from it, so a direction is mirrored by negating its y
        position.set(cameraPosition.x(), 2.f * height - cameraPosition.y(), cameraPosition.z());
        target.set(position.x() + front.x(), position.y() - front.y(), position.z() + front.z());
        up.set(cameraUp.x(), -cameraUp.y(), cameraUp.z());

        viewMatrix.identity().lookAt(position, target, up);

        // The plane of the water in the space of the mirror image (a plane is moved by the inverse transpose of the matrix that
        // moves points): its positive side, above the water, is what is kept
        viewMatrix.invert(scratch).transpose();
        plane.set(0.f, 1.f, 0.f, -height).mul(scratch);

        projectionMatrix.set(projection);
        ObliqueProjectionUtil.clip(projectionMatrix, plane);

        projectionMatrix.mul(viewMatrix, viewProjectionMatrix);
    }
}
