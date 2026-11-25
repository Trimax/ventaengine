package io.github.trimax.venta.engine.model.common.shared;

import io.github.trimax.venta.engine.model.dto.scene.SceneViewDTO;
import lombok.Getter;
import lombok.NonNull;

/**
 * How the camera sees a scene: the planes between which things are drawn and the field of view. A scene without a view has the
 * default one, which is enough for a room and not for a horizon: the far plane of a sea is thousands of units away, and the
 * nearer the near plane is, the less precise the depth of what is far.
 */
@Getter
public final class View {
    public static final float DEFAULT_NEAR = 0.1f;
    public static final float DEFAULT_FAR = 1000.f;
    public static final float DEFAULT_FOV = 60.f;

    /** The field of view must leave a perspective: not a point and not a half plane */
    public static final float MINIMAL_FOV = 1.f;
    public static final float MAXIMAL_FOV = 170.f;

    private float near = DEFAULT_NEAR;
    private float far = DEFAULT_FAR;

    /** The vertical field of view in degrees */
    private float fov = DEFAULT_FOV;

    /** The default view */
    public View() {
    }

    public View(@NonNull final SceneViewDTO dto) {
        this(dto.near() == null ? DEFAULT_NEAR : dto.near(),
                dto.far() == null ? DEFAULT_FAR : dto.far(),
                dto.fov() == null ? DEFAULT_FOV : dto.fov());
    }

    public View(final float near, final float far, final float fov) {
        set(near, far, fov);
    }

    /** Takes the view of another one, the default one when it is null */
    public void set(final View view) {
        if (view == null)
            set(DEFAULT_NEAR, DEFAULT_FAR, DEFAULT_FOV);
        else
            set(view.near, view.far, view.fov);
    }

    public void set(final float near, final float far, final float fov) {
        if (!(near > 0.f && Float.isFinite(near)))
            throw new IllegalArgumentException("The near plane must be positive, got " + near);

        if (!(far > near && Float.isFinite(far)))
            throw new IllegalArgumentException(String.format("The far plane must be farther than the near one (%s), got %s", near, far));

        if (!(fov >= MINIMAL_FOV && fov <= MAXIMAL_FOV))
            throw new IllegalArgumentException(String.format("The field of view must be from %s to %s degrees, got %s", MINIMAL_FOV, MAXIMAL_FOV, fov));

        this.near = near;
        this.far = far;
        this.fov = fov;
    }
}
