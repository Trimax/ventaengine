package io.github.trimax.venta.editor.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.editor.model.tree.Item;
import io.github.trimax.venta.engine.enums.ResourceType;
import javafx.scene.control.TreeItem;
import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {
    private final Map<ResourceType, TreeItem<Item>> groups = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private ResourceType groupSelected = ResourceType.AudioSource;

    public void reset() {
        groups.clear();
    }
    
    public void setTree(@NonNull final ResourceType type, @NonNull final TreeItem<Item> tree) {
        groups.put(type, tree);
    }

    public TreeItem<Item> getTree(@NonNull final ResourceType type) {
        if (!groups.containsKey(type))
            groups.put(type, new TreeItem<>(Item.asGroup(type)));

        return groups.get(type);
    }
}
