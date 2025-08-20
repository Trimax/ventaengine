package io.github.trimax.venta.container.tree;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public record Node<T>(String name, T value, List<Node<T>> children) {
    public boolean hasChildren() {
        return CollectionUtils.isNotEmpty(children);
    }

    public boolean hasValue() {
        return value != null;
    }
}
