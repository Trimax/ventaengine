package io.github.trimax.venta.engine.factories;

import java.util.List;

import org.apache.commons.io.FilenameUtils;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.MeshFormat;
import io.github.trimax.venta.engine.model.dto.common.Mesh;
import io.github.trimax.venta.engine.parsers.AbstractParser;
import lombok.NonNull;

@Component
public final class MeshParserFactory extends AbstractFactory<AbstractParser<Mesh>> {
    private MeshParserFactory(@NonNull final List<AbstractParser<Mesh>> parsers) {
        super(parsers, MeshParserFactory::clean);
    }

    private static void clean(final AbstractParser<Mesh> mesh) {

    }

    public AbstractParser<Mesh> get(@NonNull final String path) {
        return getBy(AbstractParser::format, MeshFormat.of(FilenameUtils.getExtension(path).toLowerCase()));
    }
}
