package io.github.trimax.venta.editor.context;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.GroupType;
import io.github.trimax.venta.engine.model.common.resource.Item;
import javafx.scene.control.TreeItem;
import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Context {
    private final Map<GroupType, TreeItem<Item>> groups = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private GroupType groupSelected = GroupType.AudioSource;

    public void reset() {
        groupSelected = GroupType.AudioSource;
        groups.clear();
    }
    
    public void setTree(@NonNull final GroupType type, @NonNull final TreeItem<Item> tree) {
        groups.put(type, tree);
    }

    public TreeItem<Item> getTree(@NonNull final GroupType type) {
        if (!groups.containsKey(type))
            groups.put(type, new TreeItem<>(Item.asGroup(type)));

        return groups.get(type);
    }
}
