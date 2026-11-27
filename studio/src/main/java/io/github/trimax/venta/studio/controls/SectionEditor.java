package io.github.trimax.venta.studio.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.trimax.venta.studio.services.SceneEditService;
import io.github.trimax.venta.studio.utils.PropertyUtil;
import io.github.trimax.venta.studio.utils.ReferenceUtil;
import io.github.trimax.venta.studio.utils.SchemaUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The fields of an object of a file (a section of the environment: the sun, the fog, ...; a file of a resource: a light), made from what
 * the object holds: a number is a field, a switch is a box, a name from a list is a list, a color is a picker, a vector is a row. A new
 * field is edited without a line of code. A change is a command of the scene, so it can be undone; the fields show the object again when
 * it changes (an undo).
 */
public final class SectionEditor extends VBox {
    private static final Set<String> COLOR_FIELDS = Set.of("r", "g", "b", "a");
    private static final List<String> VECTOR_FIELDS = List.of("x", "y", "z", "w");
    private static final int LABEL_WIDTH = 120;
    private static final double PRECISION = 10000.0;

    private final JsonSource source;
    private final List<Runnable> refreshers = new ArrayList<>();

    private boolean refreshing;

    /** The form of an object of a file; the object must be there */
    public SectionEditor(final JsonSource source) {
        super(6);
        setPadding(new Insets(4, 0, 0, 0));

        this.source = source;

        build(this, source.read(), List.of());
        refresh();
    }

    /** The form of a section of the environment of the scene that is open */
    public static SectionEditor ofSection(final SceneEditService service, final String section) {
        return new SectionEditor(new JsonSource() {
            @Override
            public JsonObject read() {
                return service.getDocument() == null || !service.getDocument().hasSection(section) ? null : service.getDocument().getSection(section);
            }

            @Override
            public void edit(final String field, final Consumer<JsonObject> change) {
                service.editSection(section, "Change", field, change);
            }

            @Override
            public Class<?> typeOf(final List<String> path) {
                return SchemaUtil.typeOf(section, path);
            }

            @Override
            public List<String> optionsOf(final List<String> path) {
                return "Skybox".equals(section) ? service.filesOf("cubemaps", List.of(".json")) : null;
            }
        });
    }

    /**
     * The form of a file of a resource of the project.
     *
     * @param type the record that the engine reads the file into ({@code LightDTO}), for the types of the fields
     */
    public static SectionEditor ofResource(final SceneEditService service, final String path, final Class<?> type) {
        return new SectionEditor(new JsonSource() {
            @Override
            public JsonObject read() {
                return service.readResource(path);
            }

            @Override
            public void edit(final String field, final Consumer<JsonObject> change) {
                service.editResource(path, "Change", field, change);
            }

            @Override
            public Class<?> typeOf(final List<String> path) {
                return SchemaUtil.typeOf(type, path);
            }

            @Override
            public List<String> optionsOf(final List<String> path) {
                final var place = path.isEmpty() ? null : ReferenceUtil.placeOf(path.getLast());
                return place == null ? null : service.filesOf(place.folder(), place.extensions());
            }
        });
    }

    /** Shows the section as the file says now */
    public void refresh() {
        refreshing = true;
        try {
            refreshers.forEach(Runnable::run);
        } finally {
            refreshing = false;
        }
    }

    /** The step of an arrow for a number of that size: a tenth of the order of the number, so a small number is not jumped over */
    public static float stepOf(final double value) {
        final var size = Math.abs(value);
        if (size >= 100.0)
            return 10.f;
        if (size >= 10.0)
            return 1.f;
        if (size >= 1.0)
            return 0.1f;

        return size >= 0.1 ? 0.01f : 0.001f;
    }

    private void build(final VBox box, final JsonObject object, final List<String> path) {
        if (isColor(object)) {
            box.getChildren().add(row("Color", color(object, path)));
            return;
        }

        for (final var entry : object.entrySet()) {
            final var child = new ArrayList<>(path);
            child.add(entry.getKey());
            final var value = entry.getValue();

            if (value.isJsonObject() && isVector(value.getAsJsonObject()))
                box.getChildren().add(vector(entry.getKey(), value.getAsJsonObject(), child));
            else if (value.isJsonObject() && isColor(value.getAsJsonObject()))
                box.getChildren().add(row(entry.getKey(), color(value.getAsJsonObject(), child)));
            else if (value.isJsonObject())
                box.getChildren().add(group(entry.getKey(), value.getAsJsonObject(), child));
            else if (value.isJsonArray())
                box.getChildren().add(list(entry.getKey(), value.getAsJsonArray(), child));
            else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean())
                box.getChildren().add(row(entry.getKey(), bool(child)));
            else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber())
                box.getChildren().add(row(entry.getKey(), number(child)));
            else if (value.isJsonPrimitive())
                box.getChildren().add(row(entry.getKey(), text(child)));
        }
    }

    /** A list of objects: every element is a group of its own, called by its number */
    private VBox list(final String title, final JsonArray array, final List<String> path) {
        final var label = new Label(PropertyUtil.humanize(title) + " (" + array.size() + ")");
        label.getStyleClass().add("panel-title");

        final var group = new VBox(6, label);
        for (int index = 0; index < array.size(); index++) {
            if (!array.get(index).isJsonObject())
                continue;

            final var child = new ArrayList<>(path);
            child.add(String.valueOf(index));

            final var element = array.get(index).getAsJsonObject();
            if (isVector(element))
                group.getChildren().add(vector(String.valueOf(index + 1), element, child));
            else
                group.getChildren().add(group(String.valueOf(index + 1), element, child));
        }

        return group;
    }

    private VBox group(final String title, final JsonObject object, final List<String> path) {
        final var label = new Label(PropertyUtil.humanize(title));
        label.getStyleClass().add("panel-title");

        final var group = new VBox(6, label);
        build(group, object, path);
        return group;
    }

    private HBox row(final String title, final javafx.scene.Node control) {
        final var label = new Label(PropertyUtil.humanize(title));
        label.setMinWidth(LABEL_WIDTH);
        label.getStyleClass().add("dim");

        final var row = new HBox(6, label, control);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    private NumberField number(final List<String> path) {
        final var whole = SchemaUtil.isWhole(source.typeOf(path));
        final var start = valueAt(path);
        final var field = new NumberField(whole ? 1.f : stepOf(start == null ? 0.0 : start.getAsDouble()), value -> {
            if (!refreshing)
                write(path, object -> setNumber(object, path, whole ? Math.round(value) : round(value)));
        });
        field.setMaxWidth(Double.MAX_VALUE);

        refreshers.add(() -> {
            final var element = valueAt(path);
            if (element != null && element.isJsonPrimitive())
                field.setValue(element.getAsFloat());
        });
        return field;
    }

    private CheckBox bool(final List<String> path) {
        final var box = new CheckBox();
        box.setOnAction(event -> {
            if (!refreshing)
                write(path, object -> parent(object, path).addProperty(last(path), box.isSelected()));
        });

        refreshers.add(() -> {
            final var element = valueAt(path);
            if (element != null && element.isJsonPrimitive())
                box.setSelected(element.getAsBoolean());
        });
        return box;
    }

    /** A name: one of the values of an enum when the engine reads the field as one, any text otherwise */
    private javafx.scene.Node text(final List<String> path) {
        final var options = source.optionsOf(path) != null ? source.optionsOf(path) : SchemaUtil.constantsOf(source.typeOf(path));
        if (options != null) {
            final var list = new ComboBox<String>();
            list.getItems().addAll(options);
            list.setMaxWidth(Double.MAX_VALUE);
            list.setOnAction(event -> {
                if (!refreshing && list.getValue() != null)
                    write(path, object -> parent(object, path).addProperty(last(path), list.getValue()));
            });

            refreshers.add(() -> {
                final var element = valueAt(path);
                if (element != null && element.isJsonPrimitive()) {
                    // A file that the folder doesn't have (or a name of another kind of path) is shown as it is
                    if (!list.getItems().contains(element.getAsString()))
                        list.getItems().add(element.getAsString());

                    list.setValue(element.getAsString());
                }
            });
            return list;
        }

        final var field = new TextField();
        final Runnable commit = () -> {
            final var element = valueAt(path);
            if (!refreshing && element != null && !field.getText().equals(element.getAsString()))
                write(path, object -> parent(object, path).addProperty(last(path), field.getText()));
        };
        field.setOnAction(event -> commit.run());
        field.focusedProperty().addListener((observable, was, is) -> {
            if (!is)
                commit.run();
        });

        refreshers.add(() -> {
            final var element = valueAt(path);
            if (element != null && element.isJsonPrimitive() && !field.isFocused())
                field.setText(element.getAsString());
        });
        return field;
    }

    private ColorPicker color(final JsonObject object, final List<String> path) {
        final var picker = new ColorPicker();
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setOnAction(event -> {
            if (refreshing)
                return;

            final var chosen = picker.getValue();
            write(path, root -> {
                final var target = at(root, path);
                target.addProperty("r", Math.round(chosen.getRed() * 1000.0) / 1000.0);
                target.addProperty("g", Math.round(chosen.getGreen() * 1000.0) / 1000.0);
                target.addProperty("b", Math.round(chosen.getBlue() * 1000.0) / 1000.0);
            });
        });

        refreshers.add(() -> {
            final var element = valueAt(path);
            if (element != null && element.isJsonObject()) {
                final var color = element.getAsJsonObject();
                picker.setValue(new Color(clamp(color, "r"), clamp(color, "g"), clamp(color, "b"), 1.0));
            }
        });
        return picker;
    }

    private VectorRow vector(final String title, final JsonObject object, final List<String> path) {
        final var keys = VECTOR_FIELDS.subList(0, object.size());
        final var step = stepOf(object.get("x").getAsDouble());
        final var row = new VectorRow(PropertyUtil.humanize(title), keys.size(), step, (axis, value) -> {
            if (!refreshing) {
                final var key = keys.get(axis);
                write(path, root -> at(root, path).addProperty(key, round(value)));
            }
        });

        refreshers.add(() -> {
            final var element = valueAt(path);
            if (element != null && element.isJsonObject()) {
                final var values = new float[keys.size()];
                for (int axis = 0; axis < values.length; axis++)
                    values[axis] = element.getAsJsonObject().get(keys.get(axis)).getAsFloat();
                row.setValues(values);
            }
        });
        return row;
    }

    /** Changes the section with a command; the steps of a drag of the same field are one command */
    private void write(final List<String> path, final Consumer<JsonObject> change) {
        source.edit(String.join(".", path), change);
    }

    private JsonElement valueAt(final List<String> path) {
        JsonElement element = source.read();
        for (final var name : path)
            element = child(element, name);

        return element;
    }

    /** The field of an object or the element of a list, null when there is none */
    private static JsonElement child(final JsonElement element, final String name) {
        if (element == null)
            return null;

        if (element.isJsonObject())
            return element.getAsJsonObject().get(name);

        if (element.isJsonArray() && name.chars().allMatch(Character::isDigit) && Integer.parseInt(name) < element.getAsJsonArray().size())
            return element.getAsJsonArray().get(Integer.parseInt(name));

        return null;
    }

    private static boolean isColor(final JsonObject object) {
        return object.size() >= 3 && object.size() <= 4 && object.has("r") && object.has("g") && object.has("b") && COLOR_FIELDS.containsAll(object.keySet());
    }

    private static boolean isVector(final JsonObject object) {
        return object.size() >= 2 && object.size() <= 4 && VECTOR_FIELDS.subList(0, object.size()).stream().allMatch(object::has)
                && object.entrySet().stream().allMatch(entry -> entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber());
    }

    private static double clamp(final JsonObject color, final String key) {
        return Math.clamp(color.get(key).getAsDouble(), 0.0, 1.0);
    }

    private static double round(final float value) {
        return Math.round(value * PRECISION) / PRECISION;
    }

    private static void setNumber(final JsonObject root, final List<String> path, final Number value) {
        parent(root, path).addProperty(last(path), value);
    }

    private static String last(final List<String> path) {
        return path.getLast();
    }

    /** The object that holds the field of the path */
    private static JsonObject parent(final JsonObject root, final List<String> path) {
        return at(root, path.subList(0, path.size() - 1));
    }

    private static JsonObject at(final JsonObject root, final List<String> path) {
        JsonElement element = root;
        for (final var name : path)
            element = child(element, name);

        return element.getAsJsonObject();
    }
}
