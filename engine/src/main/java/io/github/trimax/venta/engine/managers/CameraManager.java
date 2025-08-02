package io.github.trimax.venta.engine.managers;

import io.github.trimax.venta.engine.enums.EntityType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.model.view.CameraView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CameraManager extends AbstractManager<CameraManager.CameraEntity, CameraView> {
    private final GizmoManager.GizmoAccessor gizmoAccessor;
    private final GizmoManager gizmoManager;

    @Getter
    @Setter(onParam_ = @__(@NonNull))
    private CameraView current;

    public CameraView create(final String name) {
        log.info("Creating camera {}", name);

        return store(new CameraEntity(name, new Vector3f(0, 0, 3), new Vector3f(0, 0, 0),
                gizmoAccessor.get(gizmoManager.create("camera", GizmoType.Camera))));
    }

    @Override
    protected void destroy(final CameraEntity camera) {
        log.info("Destroying camera {} ({})", camera.getID(), camera.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.Camera;
    }

    @Getter
    public static final class CameraEntity extends AbstractEntity implements CameraView {
        private static final Vector3f worldUp = new Vector3f(0, 1, 0);

        private final Vector3f position = new Vector3f(0.f);
        private final Vector3f rotation = new Vector3f(0.f); // pitch (X), yaw (Y), roll (Z)

        private final Vector3f front = new Vector3f(0, 0, -1);
        private final Vector3f up = new Vector3f(0, 1, 0);
        private final Vector3f right = new Vector3f(1, 0, 0);

        CameraEntity(@NonNull final String name,
                final Vector3f position,
                final Vector3f target,
                @NonNull final GizmoManager.GizmoEntity gizmo) {
            super(gizmo, name);

            setPosition(position);
            lookAt(target);
        }

        private void rotateAround(final Vector3f axis, final float angleRadians) {
            final var rotationMatrix = new Matrix3f().rotate(angleRadians, axis);

            front.mul(rotationMatrix).normalize();
            up.mul(rotationMatrix).normalize();
            right.mul(rotationMatrix).normalize();
        }

        public Matrix4f getViewMatrix() {
            return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
        }

        @Override
        public void moveForward(final float distance) {
            position.fma(distance, front);
        }

        @Override
        public void moveRight(final float distance) {
            position.fma(distance, right);
        }

        @Override
        public void moveUp(final float distance) {
            position.fma(distance, worldUp);
        }

        @Override
        public void rotateYaw(final float radians) {
            rotateAround(up, radians);
        }

        @Override
        public void rotatePitch(final float radians) {
            rotateAround(right, radians);
        }

        @Override
        public void rotateRoll(final float radians) {
            rotateAround(front, radians);
        }

        @Override
        public Vector3f getPosition() {
            return new Vector3f(position);
        }

        @Override
        public void setPosition(final Vector3f newPosition) {
            this.position.set(newPosition);
        }

        @Override
        public void lookAt(final Vector3f target) {
            front.set(new Vector3f(target).sub(position).normalize());

            final var tempUp = new Vector3f(0, 1, 0);
            if (Math.abs(front.dot(tempUp)) > 0.999f)
                tempUp.set(0, 0, 1);

            right.set(new Vector3f(front).cross(tempUp).normalize());
            up.set(new Vector3f(right).cross(front).normalize());
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class CameraAccessor extends AbstractAccessor {}
}
