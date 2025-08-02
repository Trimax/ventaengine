package io.github.trimax.venta.engine.utils;

import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public final class TransformationUtil {
    public <K, T> Map<K, T> toMap(final List<T> managers, final Function<T, K> keyExtractor) {
        if (CollectionUtils.isEmpty(managers))
            return new HashMap<>();

        return StreamEx.of(managers).toMap(keyExtractor, Function.identity());
    }
}
