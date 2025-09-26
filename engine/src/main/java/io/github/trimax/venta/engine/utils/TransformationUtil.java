package io.github.trimax.venta.engine.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class TransformationUtil {
    public <K, T> Map<K, T> toMap(final List<T> managers, final Function<T, K> keyExtractor) {
        if (CollectionUtils.isEmpty(managers))
            return new HashMap<>();

        return StreamEx.of(managers).toMap(keyExtractor, Function.identity());
    }

    public <I, O> List<O> transform(final List<I> list, @NonNull final Function<I, O> transformer) {
        return CollectionUtils.isNotEmpty(list) ? StreamEx.of(list).map(transformer).toList() : Collections.emptyList();
    }
}
