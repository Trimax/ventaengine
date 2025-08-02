package io.github.trimax.venta.engine.managers.implementation;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.DrawMode;
import io.github.trimax.venta.engine.enums.GizmoType;
import io.github.trimax.venta.engine.managers.ObjectManager;
import io.github.trimax.venta.engine.model.dto.ObjectDTO;
import io.github.trimax.venta.engine.model.geo.BoundingBox;
import io.github.trimax.venta.engine.model.view.MeshView;
import io.github.trimax.venta.engine.model.view.ObjectView;
import io.github.trimax.venta.engine.model.view.ProgramView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectManagerImplementation
        extends AbstractManagerImplementation<ObjectManagerImplementation.ObjectEntity, ObjectView>
        implements ObjectManager {
    private final ResourceManagerImplementation resourceManager;
    private final ProgramManagerImplementation programManager;
    private final GizmoManagerImplementation gizmoManager;
    private final MeshManagerImplementation meshManager;

    @Override
    public ObjectView create(@NonNull final String name,
                             @NonNull final MeshView mesh,
                             @NonNull final ProgramView program) {
        log.info("Creating object {}", name);

        return store(new ObjectManagerImplementation.ObjectEntity(name,
                programManager.getEntity(program.getID()),
                meshManager.getEntity(mesh.getID()),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(0.f, 0.f, 0.f),
                new Vector3f(1.f, 1.f, 1.f),
                gizmoManager.create("Bounding box", GizmoType.Object)));
    }

    @Override
    public ObjectView load(@NonNull final String name) {
        log.info("Loading object {}", name);

        final var objectDTO = resourceManager.load(String.format("/objects/%s.json", name), ObjectDTO.class);
        return store(new ObjectManagerImplementation.ObjectEntity(name,
                programManager.getEntity(programManager.load(objectDTO.program()).getID()),
                meshManager.getEntity(meshManager.load(objectDTO.mesh()).getID()),
                objectDTO.position(),
                objectDTO.angles(),
                objectDTO.scale(),
                gizmoManager.create("Bounding box", GizmoType.Object)));
    }

    @Override
    protected void destroy(final ObjectEntity object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }

    @Override
    protected boolean shouldCache() {
        return false;
    }

    @Getter
    public static final class ObjectEntity extends AbstractEntity implements ObjectView {
        private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);
        private final BoundingBox box;

        private DrawMode drawMode = DrawMode.Polygon;
        private boolean isVisible = true;
        private boolean isLit = true;

        private ProgramManagerImplementation.ProgramEntity program;
        private MeshManagerImplementation.MeshEntity mesh;

        ObjectEntity(final String name,
                     final ProgramManagerImplementation.ProgramEntity program,
                     final MeshManagerImplementation.MeshEntity mesh,
                     final Vector3f position,
                     final Vector3f rotation,
                     final Vector3f scale,
                     final GizmoManagerImplementation.GizmoEntity gizmo) {
            super(gizmo, name);

            this.mesh = mesh;
            this.program = program;

            this.position.set(position);
            this.rotation.set(rotation);
            this.scale.set(scale);
            this.box = BoundingBox.of(mesh.getBoundingBox());
        }

        @Override
        public boolean hasProgram() {
            return program != null;
        }

        @Override
        public void setPosition(final Vector3f position) {
            this.position.set(position);
        }

        @Override
        public void setRotation(final Vector3f rotation) {
            this.rotation.set(rotation);
        }

        @Override
        public void setScale(final Vector3f scale) {
            this.scale.set(scale);
        }

        @Override
        public void move(final Vector3f offset) {
            this.position.add(offset, this.position);
        }

        @Override
        public void rotate(final Vector3f angles) {
            this.rotation.add(angles, this.rotation);
        }

        @Override
        public void scale(final Vector3f factor) {
            this.scale.add(factor, this.scale);
        }

        @Override
        public void setDrawMode(final DrawMode drawMode) {
            this.drawMode = drawMode;
        }

        @Override
        public void setLit(final boolean lit) {
            this.isLit = lit;
        }

        @Override
        public void setVisible(final boolean visible) {
            this.isVisible = visible;
        }

        @Override
        public void setProgram(final ProgramView program) {
            if (program instanceof ProgramManagerImplementation.ProgramEntity entity)
                this.program = entity;
        }

        @Override
        public void setMesh(final MeshView mesh) {
            if (mesh instanceof MeshManagerImplementation.MeshEntity entity)
                this.mesh = entity;
        }
    }
}
