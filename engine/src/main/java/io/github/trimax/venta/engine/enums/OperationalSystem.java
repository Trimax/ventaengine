package io.github.trimax.venta.engine.enums;

import lombok.NonNull;
import one.util.streamex.StreamEx;

public enum OperationalSystem {
    Windows("win", "windows"),
    Linux("nux", "nix", "aix"),
    MacOS("mac", "darwin"),
    Unknown("unknown");

    private final String[] aliases;

    OperationalSystem(@NonNull final String... aliases) {
        this.aliases = aliases;
    }

    public static OperationalSystem detect() {
        final var osName = System.getProperty("os.name").toLowerCase();

        return StreamEx.of(values())
                .filter(os -> os.matches(osName))
                .findFirst()
                .orElse(Unknown);
    }

    private boolean matches(@NonNull final String alias) {
        return StreamEx.of(aliases).anyMatch(alias::contains);
    }
}
