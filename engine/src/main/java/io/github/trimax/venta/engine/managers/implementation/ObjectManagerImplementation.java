package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.tree.Node;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.exceptions.UnknownInstanceException;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.model.common.hierarchy.MeshReference;
import io.github.trimax.venta.engine.model.common.math.Transform;
import io.github.trimax.venta.engine.model.instance.ObjectInstance;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.model.prefabs.ObjectPrefab;
import io.github.trimax.venta.engine.model.prefabs.implementation.ObjectPrefabImplementation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectManagerImplementation
        extends AbstractManagerImplementation<ObjectInstanceImplementation, ObjectInstance>
        implements ObjectManager {
    private final GizmoManagerImplementation gizmoManager;

    @Override
    public ObjectInstance create(@NonNull final String name, @NonNull final ObjectPrefab prefab) {
        if (prefab instanceof ObjectPrefabImplementation object)
            return create(name, object);

        throw new UnknownInstanceException(prefab.getClass());
    }

    private ObjectInstance create(final String name, final ObjectPrefabImplementation prefab) {
        return store(new ObjectInstanceImplementation(name,
                prefab.getProgram(),
                createHierarchy(prefab.getRoot()),
                gizmoManager.create("Bounding box", GizmoType.Object)));
    }

    private Node<MeshReference> createHierarchy(final Node<MeshReference> node) {
        return new Node<>(node.name(),
                createMesh(node.value()),
                node.hasChildren() ? StreamEx.of(node.children()).map(this::createHierarchy).toList() : null);
    }

    private MeshReference createMesh(final MeshReference reference) {
        return new MeshReference(reference.mesh(), reference.material(), new Transform(reference.transform()));
    }

    @Override
    public void delete(@NonNull final ObjectInstance instance) {
        if (instance instanceof ObjectInstanceImplementation object)
            super.delete(object);
    }

    @Override
    protected void destroy(final ObjectInstanceImplementation object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }
}
