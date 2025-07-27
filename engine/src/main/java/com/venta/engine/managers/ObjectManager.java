package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.model.dto.ObjectDTO;
import com.venta.engine.model.view.MeshView;
import com.venta.engine.model.view.ObjectView;
import com.venta.engine.model.view.ProgramView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector3f;

@Slf4j
@Component
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectManager extends AbstractManager<ObjectManager.ObjectEntity, ObjectView> {
    private final ResourceManager resourceManager;
    private final ProgramManager programManager;
    private final MeshManager meshManager;

    public ObjectView load(final String name) {
        log.info("Loading object {}", name);

        final var objectDTO = resourceManager.load(String.format("/objects/%s", name), ObjectDTO.class);
        return store(new ObjectManager.ObjectEntity(name,
                programManager.get(programManager.load(objectDTO.program()).getID()),
                meshManager.get(meshManager.load(objectDTO.mesh()).getID()),
                objectDTO.position(),
                objectDTO.angles(),
                objectDTO.scale()));
    }

    @Override
    protected void destroy(final ObjectEntity object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }

    @Getter
    public static final class ObjectEntity extends AbstractEntity implements ObjectView {
        private final String name;

        private final Vector3f position = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f rotation = new Vector3f(0.f, 0.f, 0.f);
        private final Vector3f scale = new Vector3f(1.f, 1.f, 1.f);

        private DrawMode drawMode = DrawMode.Polygon;
        private boolean isVisible = true;
        private boolean isLit = true;

        private ProgramManager.ProgramEntity program;
        private MeshManager.MeshEntity mesh;

        ObjectEntity(final String name,
                     final ProgramManager.ProgramEntity program,
                     final MeshManager.MeshEntity mesh,
                     final Vector3f position,
                     final Vector3f rotation,
                     final Vector3f scale) {
            this.name = name;
            this.mesh = mesh;
            this.program = program;

            this.position.set(position);
            this.rotation.set(rotation);
            this.scale.set(scale);
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
            if (program instanceof ProgramManager.ProgramEntity entity)
                this.program = entity;
        }

        @Override
        public void setMesh(final MeshView mesh) {
            if (mesh instanceof MeshManager.MeshEntity entity)
                this.mesh = entity;
        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ObjectAccessor extends AbstractAccessor {}
}
