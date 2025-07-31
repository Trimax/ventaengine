package io.github.trimax.venta.engine.managers;

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
    protected boolean shouldCache() {
        return false;
    }

    @Override
    protected void destroy(final CameraEntity camera) {
        log.info("Destroying camera {} ({})", camera.getID(), camera.getName());
    }

    @Getter
    public static final class CameraEntity extends AbstractEntity implements CameraView {
        private static final Vector3f worldUp = new Vector3f(0, 1, 0);

        private final Vector3f position = new Vector3f(0.f);
        private final Vector3f front = new Vector3f();
        private final Vector3f right = new Vector3f();
        private final Vector3f up = new Vector3f();

        @Getter
        private float yaw;   // around Y

        @Getter
        private float pitch; // around X

        @Getter
        private float roll;  // around Z

        CameraEntity(@NonNull final String name,
                final Vector3f position,
                final Vector3f target,
                @NonNull final GizmoManager.GizmoEntity gizmo) {
            super(gizmo, name);

            setPosition(position);
            this.front.set(new Vector3f(target).sub(position).normalize());

            // Compute angles
            this.yaw = (float) Math.atan2(front.z, front.x);
            this.pitch = (float) Math.asin(front.y);
            this.roll = 0;

            updateVectors();
        }

        private void updateVectors() {
            front.x = (float) (Math.cos(pitch) * Math.cos(yaw));
            front.y = (float) Math.sin(pitch);
            front.z = (float) (Math.cos(pitch) * Math.sin(yaw));
            front.normalize();

            right.set(front).cross(worldUp).normalize();
            up.set(right).cross(front).normalize();
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
            this.yaw += radians;

            updateVectors();
        }

        @Override
        public void rotatePitch(final float radians) {
            this.pitch = Math.clamp(this.pitch + radians, (float) Math.toRadians(-89.0f), (float) Math.toRadians(89.0f));

            updateVectors();
        }

        @Override
        public void rotateRoll(final float radians) {
            this.roll += radians;
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
            final var direction = new Vector3f(target).sub(position).normalize();

            this.yaw = (float) Math.atan2(direction.z, direction.x);
            this.pitch = (float) Math.asin(direction.y);

            updateVectors();
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class CameraAccessor extends AbstractAccessor {}
}
