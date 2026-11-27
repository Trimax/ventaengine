package io.github.trimax.venta.studio.utils;

import java.util.Collection;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Names of the things of a scene: a new thing or a copy gets a name that no other thing of its kind has.
 */
@UtilityClass
public final class NamingUtil {
    private static final Pattern NUMBER = Pattern.compile("^(.*\\S)\\s+(\\d+)$");

    /**
     * The name that the others don't have: the wanted one, or the wanted one with the next number ({@code Chest}, {@code Chest 2},
     * {@code Chest 3}). A number that the wanted name already has is the start: the copy of {@code Chest 2} is {@code Chest 3}.
     */
    public String unique(@NonNull final Collection<String> taken, @NonNull final String wanted) {
        if (!taken.contains(wanted))
            return wanted;

        final var matcher = NUMBER.matcher(wanted);
        final var base = matcher.matches() ? matcher.group(1) : wanted;

        var number = matcher.matches() ? Integer.parseInt(matcher.group(2)) + 1 : 2;
        while (taken.contains(base + " " + number))
            number++;

        return base + " " + number;
    }

    /**
     * What a thing made of a file is called at first: {@code trees/leafy-3.json} is {@code Leafy 3}.
     */
    public String nameOf(@NonNull final String resource) {
        final var file = resource.substring(resource.lastIndexOf('/') + 1);
        final var dot = file.lastIndexOf('.');
        final var name = (dot >= 0 ? file.substring(0, dot) : file).replace('-', ' ').replace('_', ' ').trim();

        return name.isEmpty() ? "Thing" : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
