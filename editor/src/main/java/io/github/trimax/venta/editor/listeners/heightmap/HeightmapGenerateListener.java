package io.github.trimax.venta.editor.listeners.heightmap;

import com.google.common.eventbus.Subscribe;
import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.context.HeightmapContext;
import io.github.trimax.venta.editor.controllers.heightmap.HeightmapGeneratorController;
import io.github.trimax.venta.editor.listeners.AbstractListener;
import io.github.trimax.venta.editor.model.event.heightmap.HeightmapGenerateEvent;
import io.github.trimax.venta.editor.utils.ImageViewUtil;
import io.github.trimax.venta.engine.utils.PerlinNoise;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeightmapGenerateListener implements AbstractListener<HeightmapGenerateEvent> {
    private final HeightmapContext context;
    private final HeightmapGeneratorController controller;

    @Override
    @Subscribe
    public void handle(final @NonNull HeightmapGenerateEvent event) {
        log.info("Generating image {}", context.getRandomSeed());
        final var heightmap = PerlinNoise.generateHeightmapWithParameters(
                context.getWidth(), context.getHeight(),
                context.getRandomSeed(), context.getCellSize(),
                context.getLevels(), context.getAttenuation(),
                context.isGroovy() ? 1 : 0); // TODO Change to spinner on UI

        final BufferedImage img = new BufferedImage(context.getWidth(), context.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = img.createGraphics();

        for (int x = 0; x < context.getWidth(); x++)
            for (int y = 0; y < context.getHeight(); y++) {
                final var value = (int) heightmap[x][y];
                g.setColor(new java.awt.Color(value, value, value, 255));
                g.drawLine(x, y, x, y);
            }

        context.setHeightmap(img);
        ImageViewUtil.setImage(img, controller.heightmapImageView);
    }
}
