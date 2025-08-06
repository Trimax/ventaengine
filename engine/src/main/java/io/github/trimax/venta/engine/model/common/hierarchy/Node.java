package io.github.trimax.venta.engine.model.common.hierarchy;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public record Node<T>(String name, T value, List<Node<T>> children) {
    public boolean hasChildren() {
        return CollectionUtils.isNotEmpty(children);
    }

    public boolean hasValue() {
        return value != null;
    }
}
