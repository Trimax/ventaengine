package io.github.trimax.venta.engine.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;

import io.github.trimax.venta.engine.executors.AbstractExecutor;
import lombok.experimental.UtilityClass;
import one.util.streamex.StreamEx;

@UtilityClass
public final class TransformationUtil {
    public <E extends AbstractExecutor> Map<String, E> toMap(final List<E> executors) {
        if (CollectionUtils.isEmpty(executors))
            return new HashMap<>();

        return StreamEx.of(executors).toMap(AbstractExecutor::getCommand, Function.identity());
    }
}
