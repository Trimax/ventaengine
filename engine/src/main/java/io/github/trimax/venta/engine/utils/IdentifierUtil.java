package io.github.trimax.venta.engine.utils;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public final class IdentifierUtil {
    private static final String BASE57 = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final Set<String> generatedIDs = new HashSet<>();
    private static final SecureRandom random = new SecureRandom();

    public String generate(final int length) {
        String id;
        do {
            id = generateID(length);
        } while (generatedIDs.contains(id));

        generatedIDs.add(id);
        return id;
    }

    private String generateID(final int length) {
        final var builder = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            builder.append(BASE57.charAt(random.nextInt(BASE57.length())));

        return builder.toString();
    }
}
