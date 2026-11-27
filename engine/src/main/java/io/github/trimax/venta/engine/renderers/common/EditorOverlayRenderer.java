package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.helpers.ScreenPassHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.geo.BoundingBox;
import io.github.trimax.venta.engine.model.common.overlay.EditorOverlay;
import io.github.trimax.venta.engine.model.common.overlay.Mark;
import io.github.trimax.venta.engine.model.instance.implementation.CameraInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.instance.implementation.SceneInstanceImplementation;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import io.github.trimax.venta.engine.utils.GizmoUtil;
import io.github.trimax.venta.engine.utils.OverlayUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Draws what an editor puts over the finished image of the scene (see {@link EditorOverlay}): the grid of the ground, the marks of the
 * things without a shape, and the frame of the thing that is chosen. It is drawn after the post processing, so the exposure and the
 * smoothing don't touch it, and it reads the depth of the scene, so what is in front of it hides it (a line that is hidden is faint).
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EditorOverlayRenderer {
    private static final int MAXIMAL_SEGMENTS = 96;
    private static final float[] SELECTION_COLOR = {1.f, 0.62f, 0.12f};
    private static final float MARK_ALPHA = 0.95f;
    private static final float HIDDEN_ALPHA = 0.28f;

    /** How much of an edge of the frame is drawn at every corner of it */
    private static final float BRACKET = 0.25f;

    /** The distance to the camera at which the grid is gone */
    private static final float GRID_DISTANCE = 400.f;

    private final ProgramRegistryImplementation programRegistry;
    private final Memory memory;

    private ScreenPassHelper screenPass;

    /** Draws the overlay of the scene; the finished image is in the framebuffer that is bound, the depth of the scene is in the texture */
    public void render(@NonNull final SceneInstanceImplementation scene, @NonNull final CameraInstanceImplementation camera,
                       @NonNull final WindowState window, @NonNull final EditorOverlay overlay, final int depthTextureID) {
        final var view = camera.getViewMatrix();
        final var projection = window.getProjectionMatrix();

        final var viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);

        if (screenPass == null)
            screenPass = new ScreenPassHelper(memory);

        try (final var _ = screenPass.begin()) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, depthTextureID);

            if (overlay.isGrid())
                drawGrid(view, projection, camera.getPosition());

            drawLines(scene, camera, overlay, view, projection, viewport[2], viewport[3], window.getView().getNear());

            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
        }
    }

    /** Deletes the vertex array of the passes, the next frame makes it again */
    public void release() {
        if (screenPass != null)
            screenPass.release();
    }

    private void drawGrid(final Matrix4fc view, final Matrix4fc projection, final Vector3fc cameraPosition) {
        final var program = programRegistry.get(ProgramType.EditorGrid.getProgramName()).getInternalID();
        glUseProgram(program);

        try (final var stack = stackPush()) {
            final var buffer = stack.mallocFloat(16);
            new Matrix4f(projection).mul(view).invert().get(buffer);
            glUniformMatrix4fv(glGetUniformLocation(program, "inverseViewProjection"), false, buffer);
        }

        glUniform3f(glGetUniformLocation(program, "cameraPosition"), cameraPosition.x(), cameraPosition.y(), cameraPosition.z());
        glUniform1f(glGetUniformLocation(program, "fadeDistance"), GRID_DISTANCE);
        glUniform1i(glGetUniformLocation(program, "textureDepth"), 0);

        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void drawLines(final SceneInstanceImplementation scene, final CameraInstanceImplementation camera, final EditorOverlay overlay,
                           final Matrix4fc view, final Matrix4fc projection, final int width, final int height, final float near) {
        final var program = programRegistry.get(ProgramType.EditorLines.getProgramName()).getInternalID();
        glUseProgram(program);

        try (final var stack = stackPush()) {
            final var buffer = stack.mallocFloat(16);
            glUniformMatrix4fv(glGetUniformLocation(program, "viewMatrix"), false, view.get(buffer));
            glUniformMatrix4fv(glGetUniformLocation(program, "projectionMatrix"), false, projection.get(buffer));
        }

        glUniform2f(glGetUniformLocation(program, "viewportSize"), width, height);
        glUniform1f(glGetUniformLocation(program, "thickness"), Math.max(2.f, height / 450.f));
        glUniform1f(glGetUniformLocation(program, "nearPlane"), near);
        glUniform1i(glGetUniformLocation(program, "textureDepth"), 0);

        final var batch = new Batch(program);
        final var marks = OverlayUtil.marks(scene);

        if (overlay.isMarks())
            marks.forEach(mark -> addMark(batch, mark, camera.getPosition(), mark.kind().getColor(), 1.f));

        final var selected = overlay.getSelected();
        if (selected instanceof ObjectInstanceImplementation object)
            addBrackets(batch, object.getBox(), object.getTransform().getMatrix());
        else if (selected != null)
            marks.stream().filter(mark -> mark.instance() == selected).forEach(mark -> addMark(batch, mark, camera.getPosition(), SELECTION_COLOR, 1.6f));

        batch.flush();

        if (overlay.getGizmoMode() != null) {
            // The handles are not dimmed by what is in front of them
            glUniform1f(glGetUniformLocation(program, "hiddenAlpha"), 1.f);

            final var size = GizmoUtil.size(overlay.getGizmoOrigin(), camera.getPosition());
            for (final var line : GizmoUtil.lines(overlay.getGizmoMode(), overlay.getGizmoOrigin(), overlay.getGizmoAxes(), size, overlay.getGizmoHandle()))
                batch.add(line.start(), line.end(), line.color(), 1.f);

            batch.flush();
            glUniform1f(glGetUniformLocation(program, "hiddenAlpha"), HIDDEN_ALPHA);
        }
    }

    /** A cross of three lines at the place of the mark: of the same size on the screen whatever the distance */
    private void addMark(final Batch batch, final Mark mark, final Vector3fc camera, final float[] color, final float scale) {
        final var position = mark.position();
        final var size = OverlayUtil.markSize(position, camera) * scale;

        batch.add(new Vector3f(position).sub(size, 0.f, 0.f), new Vector3f(position).add(size, 0.f, 0.f), color, MARK_ALPHA);
        batch.add(new Vector3f(position).sub(0.f, size, 0.f), new Vector3f(position).add(0.f, size, 0.f), color, MARK_ALPHA);
        batch.add(new Vector3f(position).sub(0.f, 0.f, size), new Vector3f(position).add(0.f, 0.f, size), color, MARK_ALPHA);
    }

    /** The frame of a box that is moved, turned and scaled: a short part of every edge at both of its ends */
    private void addBrackets(final Batch batch, final BoundingBox box, final Matrix4fc model) {
        final var corners = new Vector3f[8];
        for (int index = 0; index < 8; index++)
            corners[index] = model.transformPosition(new Vector3f(
                    (index & 1) == 0 ? box.min().x : box.max().x,
                    (index & 2) == 0 ? box.min().y : box.max().y,
                    (index & 4) == 0 ? box.min().z : box.max().z));

        // The corners differ in one bit along an edge of the box
        for (int index = 0; index < 8; index++)
            for (int bit = 1; bit <= 4; bit <<= 1) {
                if ((index & bit) != 0)
                    continue;

                final var from = corners[index];
                final var to = corners[index | bit];
                final var step = new Vector3f(to).sub(from).mul(BRACKET);

                batch.add(from, new Vector3f(from).add(step), SELECTION_COLOR, 1.f);
                batch.add(to, new Vector3f(to).sub(step), SELECTION_COLOR, 1.f);
            }
    }

    /**
     * The segments that wait for a draw: the uniforms of the program hold {@link #MAXIMAL_SEGMENTS}, so a longer list is drawn in parts.
     */
    private static final class Batch {
        private final int program;
        private final float[] starts = new float[MAXIMAL_SEGMENTS * 3];
        private final float[] ends = new float[MAXIMAL_SEGMENTS * 3];
        private final float[] colors = new float[MAXIMAL_SEGMENTS * 4];
        private int count;

        private Batch(final int program) {
            this.program = program;
        }

        private void add(final Vector3fc start, final Vector3fc end, final float[] color, final float alpha) {
            if (count == MAXIMAL_SEGMENTS)
                flush();

            start.get(starts, count * 3);
            end.get(ends, count * 3);
            colors[count * 4] = color[0];
            colors[count * 4 + 1] = color[1];
            colors[count * 4 + 2] = color[2];
            colors[count * 4 + 3] = alpha;
            count++;
        }

        private void flush() {
            if (count == 0)
                return;

            glUniform3fv(glGetUniformLocation(program, "segmentStart"), starts);
            glUniform3fv(glGetUniformLocation(program, "segmentEnd"), ends);
            glUniform4fv(glGetUniformLocation(program, "segmentColor"), colors);
            glDrawArrays(GL_TRIANGLES, 0, count * 6);

            count = 0;
        }
    }
}
