package io.github.trimax.venta.engine.model.entity.implementation;

import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.stb.STBTTBakedChar;

import static io.github.trimax.venta.engine.definitions.Definitions.*;

@Slf4j
@Value
public class AtlasEntityImplementation extends AbstractEntityImplementation implements AtlasEntity {
    @NonNull
    TextureEntityImplementation texture;

    @NonNull
    STBTTBakedChar.Buffer buffer;

    AtlasEntityImplementation(@NonNull final TextureEntityImplementation texture,
                              @NonNull final STBTTBakedChar.Buffer buffer) {
        this.texture = texture;
        this.buffer = buffer;

        printUsageSummary();
    }

    private void printUsageSummary() {
        int maxX = 0;
        int maxY = 0;
        int usedChars = 0;

        for (int i = 0; i < FONT_ATLAS_CHARACTERS_COUNT; i++) {
            final var backedCharacter = buffer.get(i);
            final var width = backedCharacter.x1() - backedCharacter.x0();
            final var height = backedCharacter.y1() - backedCharacter.y0();
            if (width > 0 && height > 0) {
                usedChars++;
                if (backedCharacter.x1() > maxX)
                    maxX = backedCharacter.x1();

                if (backedCharacter.y1() > maxY)
                    maxY = backedCharacter.y1();
            }
        }

        log.debug("Atlas: used chars = {}, maxX = {}, maxY = {} (atlas size: {} x {}). Used: {}%",
                usedChars, maxX, maxY, FONT_ATLAS_WIDTH, FONT_ATLAS_HEIGHT,
                String.format("%2.2f", 100.f * (float) (maxX * maxY) / (float) (FONT_ATLAS_WIDTH * FONT_ATLAS_HEIGHT)));
    }
}