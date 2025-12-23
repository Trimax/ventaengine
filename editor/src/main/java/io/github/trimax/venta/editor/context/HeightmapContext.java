package io.github.trimax.venta.editor.context;

import io.github.trimax.venta.container.annotations.Component;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.image.BufferedImage;

@Component
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapContext {
    private int randomSeed = 1;
    private int width = 512;
    private int height = 512;
    private double cellSize = 16.0;
    private int levels = 4;
    private double attenuation = 0.5;
    private boolean groovy = false;
    
    private BufferedImage heightmap = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
}
