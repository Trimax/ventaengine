package io.github.trimax.venta.studio.model.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * What the studio remembers between runs: the projects that were open, and the size of the window.
 *
 * @param recentProjects the folders of the projects, the last one first
 * @param windowWidth    the width of the window, 0 for the default
 * @param windowHeight   the height of the window, 0 for the default
 */
public record StudioSettings(List<String> recentProjects, int windowWidth, int windowHeight) {
    public static final int MAXIMAL_RECENT_PROJECTS = 10;
    public static final StudioSettings DEFAULT = new StudioSettings(List.of(), 0, 0);

    public StudioSettings {
        recentProjects = recentProjects == null ? List.of() : List.copyOf(recentProjects);
    }

    /** The project that was open last, or null when there was none */
    public String lastProject() {
        return recentProjects.isEmpty() ? null : recentProjects.getFirst();
    }

    /** The settings after a project is opened: it is the first of the recent ones, once */
    public StudioSettings withProject(final String folder) {
        final var projects = new ArrayList<String>(recentProjects.size() + 1);
        projects.add(folder);
        recentProjects.stream().filter(project -> !project.equals(folder)).forEach(projects::add);

        return new StudioSettings(List.copyOf(projects.subList(0, Math.min(projects.size(), MAXIMAL_RECENT_PROJECTS))), windowWidth, windowHeight);
    }

    /** The settings without a project: it is gone from the disk or the user removed it from the list */
    public StudioSettings withoutProject(final String folder) {
        final List<String> projects = recentProjects.stream().filter(project -> !project.equals(folder)).toList();
        return new StudioSettings(projects, windowWidth, windowHeight);
    }

    public StudioSettings withWindow(final int width, final int height) {
        return new StudioSettings(recentProjects, width, height);
    }
}
