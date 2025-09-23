package io.github.trimax.venta.engine.model.entity.implementation;

import org.lwjgl.stb.STBTTBakedChar;

import io.github.trimax.venta.engine.definitions.DefinitionsFont;
import io.github.trimax.venta.engine.model.entity.AtlasEntity;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

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

        for (int i = 0; i < DefinitionsFont.ATLAS_COUNT_CHARACTERS; i++) {
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
                usedChars, maxX, maxY, DefinitionsFont.ATLAS_WIDTH, DefinitionsFont.ATLAS_HEIGHT,
                String.format("%2.2f", 100.f * (float) (maxX * maxY) / (float) (DefinitionsFont.ATLAS_WIDTH * DefinitionsFont.ATLAS_HEIGHT)));
    }
}