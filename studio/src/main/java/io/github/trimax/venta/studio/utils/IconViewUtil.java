package io.github.trimax.venta.studio.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.trimax.venta.engine.enums.GroupType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * The small pictures of the panels: the icons of the folders of the groups of resources and of files. A picture is loaded once.
 */
@UtilityClass
public final class IconViewUtil {
    public static final int SIZE = 14;

    private static final String FOLDER = "/icons/common/folder.png";
    private static final String FILE = "/icons/common/file.png";

    /** The size of the picture of an image file, and how many of them are kept */
    public static final int THUMBNAIL_SIZE = 28;
    private static final int THUMBNAILS_KEPT = 400;

    private static final Map<String, Image> IMAGES = new ConcurrentHashMap<>();

    /** The pictures of the image files, the ones that were looked at least recently are forgotten first */
    private static final Map<String, Image> THUMBNAILS = new java.util.LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, Image> eldest) {
            return size() > THUMBNAILS_KEPT;
        }
    };

    /** The icon of a folder of the project: of its group of resources, or the plain one */
    public ImageView folder(final Path project, @NonNull final Path folder) {
        final GroupType group = project == null || folder.equals(project) ? null : ProjectUtil.groupOf(project, folder);

        return icon(group == null ? FOLDER : group.getIconPath());
    }

    public ImageView file() {
        return icon(FILE);
    }

    /**
     * The icon of a file of the project: a small picture of its contents for an image (loaded in the background, once for a file that
     * has not changed), the plain icon for the others.
     */
    public ImageView file(@NonNull final Path file) {
        if (ProjectUtil.previewOf(file) != ProjectUtil.Preview.Image)
            return file();

        final var key = file.toAbsolutePath() + "#" + file.toFile().lastModified();
        final Image image;
        synchronized (THUMBNAILS) {
            image = THUMBNAILS.computeIfAbsent(key, ignored -> new Image(file.toUri().toString(), THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true, true));
        }

        final var view = new ImageView(image);
        view.setFitWidth(THUMBNAIL_SIZE);
        view.setFitHeight(THUMBNAIL_SIZE);
        view.setPreserveRatio(true);

        return view;
    }

    /** The picture of a resource of the studio, in the size of the icons; without a picture it is empty */
    public ImageView icon(@NonNull final String resource) {
        final var view = new ImageView(IMAGES.computeIfAbsent(resource, IconViewUtil::load));
        view.setFitWidth(SIZE);
        view.setFitHeight(SIZE);

        return view;
    }

    private Image load(final String resource) {
        try (final InputStream stream = IconViewUtil.class.getResourceAsStream(resource)) {
            return stream == null ? null : new Image(stream);
        } catch (final IOException exception) {
            return null;
        }
    }
}
