package io.github.trimax.venta.studio;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_BGRA;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.lwjgl.system.MemoryStack;

import io.github.trimax.venta.engine.context.VentaContext;
import io.github.trimax.venta.engine.core.Engine;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import lombok.Getter;

/**
 * The view of the scene: the engine draws a frame on every pulse of JavaFX into its hidden window, the finished image is read
 * from the window and shown in an image. Everything runs on the JavaFX thread, which is the main thread of the application on
 * macOS, so GLFW works.
 *
 * <p>The image has the size of the panel in the pixels of the screen (the engine window is resized to it once the panel stops
 * changing), and it is upside down in memory (OpenGL starts from the bottom), so the image view is turned over.
 */
public final class ViewportPanel extends Region {
    /** The frames that the size of the panel must stay the same before the engine window follows it */
    private static final int RESIZE_DELAY = 6;

    /** The pixels the mouse may move while a button is down and it is still a click */
    private static final double CLICK_TOLERANCE = 4.0;

    private final Engine engine;
    private final VentaContext context;
    private final FlyCamera flyCamera;
    private final ImageView view = new ImageView();
    private final AnimationTimer timer;

    private ByteBuffer pixels;
    private PixelBuffer<ByteBuffer> buffer;
    private int imageWidth;
    private int imageHeight;

    /** The size the engine window is asked to have, in pixels, and how long it has stayed the same */
    private int requestedWidth = StudioEngineApplication.INITIAL_WIDTH;
    private int requestedHeight = StudioEngineApplication.INITIAL_HEIGHT;
    private int requestedFor;
    private int engineWidth = StudioEngineApplication.INITIAL_WIDTH;
    private int engineHeight = StudioEngineApplication.INITIAL_HEIGHT;

    private double mouseX;
    private double mouseY;

    /** Where the primary button went down, and whether the mouse moved far enough since for it to be a drag and not a click */
    private double pressedX;
    private double pressedY;
    private boolean dragged;

    private BiConsumer<Float, Float> onPick = (x, y) -> { };
    private Runnable onFocus = () -> { };
    private Runnable onDelete = () -> { };
    private Predicate<Path> dropAcceptor = file -> false;
    private DropHandler onDrop = (file, x, y) -> { };
    private long lastPulse;

    /** What the last frames cost, for the status line (the averages of the last second) */
    @Getter
    private final FrameStatistics statistics = new FrameStatistics();
    private long readNanoseconds;

    public ViewportPanel(final Engine engine, final VentaContext context, final FlyCamera flyCamera) {
        this.engine = engine;
        this.context = context;
        this.flyCamera = flyCamera;

        view.setScaleY(-1.0);
        view.setSmooth(true);
        view.setPreserveRatio(false);
        getChildren().add(view);

        setFocusTraversable(true);
        setStyle("-fx-background-color: black;");
        setMinSize(200, 150);

        addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        setOnDragOver(event -> {
            if (droppedFile(event.getDragboard()) != null)
                event.acceptTransferModes(TransferMode.COPY);

            event.consume();
        });
        setOnDragDropped(event -> {
            final var file = droppedFile(event.getDragboard());
            if (file != null && getWidth() > 0 && getHeight() > 0) {
                final var point = sceneToLocal(event.getSceneX(), event.getSceneY());
                onDrop.drop(file, (float) (point.getX() / getWidth()), (float) (point.getY() / getHeight()));
            }

            event.setDropCompleted(file != null);
            event.consume();
        });
        addEventHandler(ScrollEvent.SCROLL, event -> flyCamera.dolly(event.getDeltaY() * 0.02));
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F && !event.isShortcutDown())
                onFocus.run();
            else if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)
                onDelete.run();
            else
                flyCamera.press(event.getCode());

            event.consume();
        });
        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            flyCamera.release(event.getCode());
            event.consume();
        });
        focusedProperty().addListener((observable, was, is) -> {
            if (!is)
                flyCamera.releaseAll();
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                pulse(now);
            }
        };
    }

    /** A click in the viewport: the point is from 0 to 1 over the viewport, from its top left corner */
    public void setOnPick(final BiConsumer<Float, Float> onPick) {
        this.onPick = onPick;
    }

    /** The key that deletes the chosen thing is pressed */
    public void setOnDelete(final Runnable onDelete) {
        this.onDelete = onDelete;
    }

    /**
     * What is dropped on the viewport: which files it takes, and what it does with a file that is dropped.
     */
    public void setDrop(final Predicate<Path> acceptor, final DropHandler handler) {
        this.dropAcceptor = acceptor;
        this.onDrop = handler;
    }

    /**
     * A file is dropped on the viewport.
     */
    @FunctionalInterface
    public interface DropHandler {
        /**
         * The file is dropped at a point of the viewport.
         *
         * @param x the point from 0 (the left edge) to 1 (the right one)
         * @param y the point from 0 (the top edge) to 1 (the bottom one)
         */
        void drop(Path file, float x, float y);
    }

    /** The key that turns the camera to the chosen thing is pressed */
    public void setOnFocus(final Runnable onFocus) {
        this.onFocus = onFocus;
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    /** The size of the panel in the pixels of the screen */
    private void measure() {
        final var scale = getScene() != null && getScene().getWindow() != null ? getScene().getWindow().getOutputScaleX() : 1.0;
        final var width = Math.max(1, (int) Math.round(getWidth() * scale));
        final var height = Math.max(1, (int) Math.round(getHeight() * scale));

        if (width != requestedWidth || height != requestedHeight) {
            requestedWidth = width;
            requestedHeight = height;
            requestedFor = 0;
        }
    }

    @Override
    protected void layoutChildren() {
        view.setFitWidth(getWidth());
        view.setFitHeight(getHeight());
        view.relocate(0, 0);
    }

    private void pulse(final long now) {
        final var seconds = lastPulse == 0 ? 0.f : (now - lastPulse) / 1e9f;
        lastPulse = now;

        measure();
        if ((requestedWidth != engineWidth || requestedHeight != engineHeight) && ++requestedFor >= RESIZE_DELAY) {
            engine.resize(requestedWidth, requestedHeight);
            engineWidth = requestedWidth;
            engineHeight = requestedHeight;
        }

        flyCamera.update(seconds);
        flyCamera.apply(context.getCameraManager().getCurrent());

        readNanoseconds = 0;
        final var started = System.nanoTime();
        engine.renderFrame();
        statistics.add(seconds, System.nanoTime() - started, readNanoseconds, imageWidth, imageHeight);
    }

    /** The engine has drawn a frame: its image is in the framebuffer of the hidden window */
    public void onFrame(final int width, final int height) {
        final var started = System.nanoTime();

        if (pixels == null || width != imageWidth || height != imageHeight) {
            imageWidth = width;
            imageHeight = height;
            pixels = ByteBuffer.allocateDirect(width * height * 4);
            buffer = new PixelBuffer<>(width, height, pixels, PixelFormat.getByteBgraPreInstance());
            view.setImage(new WritableImage(buffer));
        }

        // The post pass leaves its own alpha: the image must be opaque
        try (final var stack = MemoryStack.stackPush()) {
            final FloatBuffer clear = stack.mallocFloat(4);
            glGetFloatv(GL_COLOR_CLEAR_VALUE, clear);
            glColorMask(false, false, false, true);
            glClearColor(0.f, 0.f, 0.f, 1.f);
            glClear(GL_COLOR_BUFFER_BIT);
            glColorMask(true, true, true, true);
            glClearColor(clear.get(0), clear.get(1), clear.get(2), clear.get(3));
        }

        glReadBuffer(GL_BACK);
        pixels.clear();
        glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_BYTE, pixels);
        buffer.updateBuffer(ignored -> null);

        readNanoseconds = System.nanoTime() - started;
    }

    /** The file that is dragged over the viewport when the viewport takes it, null otherwise */
    private Path droppedFile(final Dragboard board) {
        if (!board.hasFiles() || board.getFiles().isEmpty())
            return null;

        final var file = board.getFiles().getFirst().toPath();
        return dropAcceptor.test(file) ? file : null;
    }

    private void onMousePressed(final MouseEvent event) {
        requestFocus();
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();
        pressedX = mouseX;
        pressedY = mouseY;
        dragged = false;
    }

    /** A click of the primary button that didn't move the mouse chooses what is under it */
    private void onMouseReleased(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && !dragged && !event.isAltDown() && getWidth() > 0 && getHeight() > 0) {
            final var point = sceneToLocal(event.getSceneX(), event.getSceneY());
            onPick.accept((float) (point.getX() / getWidth()), (float) (point.getY() / getHeight()));
        }
    }

    private void onMouseDragged(final MouseEvent event) {
        final var deltaX = event.getSceneX() - mouseX;
        final var deltaY = event.getSceneY() - mouseY;
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();

        if (Math.hypot(event.getSceneX() - pressedX, event.getSceneY() - pressedY) > CLICK_TOLERANCE)
            dragged = true;

        if (event.getButton() == MouseButton.SECONDARY)
            flyCamera.look(deltaX, deltaY);
        else if (event.getButton() == MouseButton.MIDDLE)
            flyCamera.pan(deltaX, deltaY);
        else if (event.getButton() == MouseButton.PRIMARY && event.isAltDown())
            flyCamera.orbit(deltaX, deltaY);
    }

    /** The averages of what the frames cost over about a second */
    public static final class FrameStatistics {
        private static final double WINDOW = 1.0;

        private double accumulated;
        private long frameNanoseconds;
        private long readNanoseconds;
        private int frames;

        @Getter
        private double framesPerSecond;

        @Getter
        private double frameMilliseconds;

        @Getter
        private double readMilliseconds;

        @Getter
        private String size = "";

        void add(final double wallSeconds, final long frame, final long read, final int width, final int height) {
            accumulated += wallSeconds;
            frameNanoseconds += frame;
            readNanoseconds += read;
            frames++;

            if (accumulated >= WINDOW) {
                framesPerSecond = frames / accumulated;
                frameMilliseconds = frameNanoseconds / 1e6 / frames;
                readMilliseconds = readNanoseconds / 1e6 / frames;
                size = width + "x" + height;

                accumulated = 0;
                frameNanoseconds = 0;
                readNanoseconds = 0;
                frames = 0;
            }
        }
    }
}
