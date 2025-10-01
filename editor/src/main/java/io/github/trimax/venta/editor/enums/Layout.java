package io.github.trimax.venta.editor.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Layout {
    Main("Venta Editor", "/layouts/main.fxml"),
    Mixer("Texture mixer", "/tools/mixer/main.fxml"),
    Packer("Texture packer", "/tools/packer/main.fxml"),
    Generator("Heightmap Generator", "/tools/generator/main.fxml");

    private final String title;
    private final String path;
}
