package io.github.trimax.venta.studio.controls;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.google.gson.JsonObject;

import io.github.trimax.venta.studio.model.outline.OutlineKind;
import io.github.trimax.venta.studio.services.SceneEditService;
import io.github.trimax.venta.studio.utils.EntryUtil;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * The fields of a thing of the scene that can be changed: its name and its place, angles and size, whichever it has. A change is a command
 * of the scene, so it can be undone; what the scene shows is shown in the fields again when it changes (an undo).
 */
public final class EntryEditor extends VBox {
    private static final Vector3f ZERO = new Vector3f();
    private static final Vector3f ONE = new Vector3f(1.f);
    private static final Vector2f ONE_BY_ONE = new Vector2f(1.f);

    private static final float POSITION_STEP = 0.1f;
    private static final float ANGLE_STEP = 1.f;
    private static final float SCALE_STEP = 0.05f;

    private final SceneEditService service;
    private final OutlineKind kind;
    private final int index;

    private final TextField name = new TextField();
    private final VectorRow position;
    private final VectorRow angles;
    private final VectorRow scale;
    private final VectorRow direction;
    private final VectorRow size;

    /**
     * The fields of a thing of the scene.
     *
     * @param file what the thing is made from, shown under its name
     */
    public EntryEditor(final SceneEditService service, final OutlineKind kind, final int index, final String file) {
        super(6);
        setPadding(new Insets(4, 0, 0, 0));

        this.service = service;
        this.kind = kind;
        this.index = index;

        name.setPromptText("Name");
        name.setOnAction(event -> rename());
        name.focusedProperty().addListener((observable, was, is) -> {
            if (!is)
                rename();
        });

        final var source = new Label(file);
        source.getStyleClass().add("dim");
        getChildren().addAll(name, source, new Label("Transform"));
        getChildren().getLast().getStyleClass().add("panel-title");

        position = vector("Position", EntryUtil.POSITION, POSITION_STEP, "Move", 3, ZERO, 1.f);
        getChildren().add(position);

        final var transformable = kind == OutlineKind.Object || kind == OutlineKind.Terrain || kind == OutlineKind.Water;
        angles = transformable ? vector("Rotation", EntryUtil.ANGLES, ANGLE_STEP, "Rotate", 3, ZERO, (float) (180.0 / Math.PI)) : null;
        scale = transformable ? vector("Scale", EntryUtil.SCALE, SCALE_STEP, "Scale", 3, ONE, 1.f) : null;
        direction = kind == OutlineKind.Light ? vector("Direction", EntryUtil.DIRECTION, 0.05f, "Turn", 3, ZERO, 1.f) : null;
        size = kind == OutlineKind.Billboard ? vector("Size", EntryUtil.SIZE, SCALE_STEP, "Resize", 2, ONE_BY_ONE, 1.f) : null;

        for (final var row : new VectorRow[]{angles, scale, direction, size})
            if (row != null)
                getChildren().add(row);

        refresh();
    }

    /** Shows the thing as the file says now */
    public void refresh() {
        final var entry = service.getDocument().getEntry(kind, index);

        if (!name.isFocused())
            name.setText(EntryUtil.getName(entry));

        position.setValues(components(EntryUtil.getVector(entry, EntryUtil.POSITION, ZERO), 1.f));
        if (angles != null)
            angles.setValues(components(EntryUtil.getVector(entry, EntryUtil.ANGLES, ZERO), (float) (180.0 / Math.PI)));
        if (scale != null)
            scale.setValues(components(EntryUtil.getVector(entry, EntryUtil.SCALE, ONE), 1.f));
        if (direction != null)
            direction.setValues(components(EntryUtil.getVector(entry, EntryUtil.DIRECTION, ZERO), 1.f));
        if (size != null)
            size.setValues(EntryUtil.getVector(entry, EntryUtil.SIZE, ONE_BY_ONE).x, EntryUtil.getVector(entry, EntryUtil.SIZE, ONE_BY_ONE).y);
    }

    private void rename() {
        final var text = name.getText() == null ? "" : name.getText().trim();
        if (text.isEmpty() || text.equals(EntryUtil.getName(service.getDocument().getEntry(kind, index))))
            return;

        service.edit(kind, index, "Rename", "name", entry -> EntryUtil.setName(entry, text));
    }

    /**
     * A row of the vector of a field of the thing.
     *
     * @param unit how many of the units of the row are in a unit of the file: the angles are in degrees on the screen, in radians in the file
     */
    private VectorRow vector(final String title, final String field, final float step, final String action, final int count, final Object fallback, final float unit) {
        return new VectorRow(title, count, step, (axis, value) -> service.edit(kind, index, action, field, entry -> {
            if (count == 2) {
                final var vector = EntryUtil.getVector(entry, field, (Vector2f) fallback);
                vector.setComponent(axis, value / unit);
                EntryUtil.setVector(entry, field, vector);
            } else {
                final var vector = EntryUtil.getVector(entry, field, (Vector3f) fallback);
                vector.setComponent(axis, value / unit);
                EntryUtil.setVector(entry, field, vector);
            }
        }));
    }

    private float[] components(final Vector3f vector, final float unit) {
        return new float[]{vector.x * unit, vector.y * unit, vector.z * unit};
    }

    /** The fields of the thing in the file (for tests and for the inspector, which asks what it shows) */
    public JsonObject getEntry() {
        return service.getDocument().getEntry(kind, index);
    }
}
