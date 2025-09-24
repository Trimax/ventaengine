package io.github.trimax.venta.editor.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.editor.model.tree.ResourceType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {
    private final Map<ResourceType, Node<Item>> trees = new ConcurrentHashMap<>();

    public void reset() {
        trees.clear();
    }

    public Node<Item> getTree(@NonNull final ResourceType type) {
        if (!trees.containsKey(type))
            trees.put(type, new Node<>(type.name(), null, null));

        return trees.get(type);
    }
}
