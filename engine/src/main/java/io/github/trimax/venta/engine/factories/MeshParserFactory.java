package io.github.trimax.venta.engine.factories;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.dto.MeshDTO;
import io.github.trimax.venta.engine.parsers.AbstractParser;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

@Component
public final class MeshParserFactory extends AbstractFactory<AbstractParser<MeshDTO>> {
    private MeshParserFactory(@NonNull final List<AbstractParser<MeshDTO>> parsers) {
        super(parsers, MeshParserFactory::clean);
    }

    private static void clean(final AbstractParser<MeshDTO> mesh) {

    }

    public AbstractParser<MeshDTO> get(@NonNull final String path) {
        return getBy(AbstractParser::format, MeshFormat.of(FilenameUtils.getExtension(path).toLowerCase()));
    }
}
