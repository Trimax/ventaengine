package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.DrawMode;
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
    @Override
    protected void destroy(final ObjectEntity object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }

    @Getter
    public static final class ObjectEntity extends AbstractEntity implements ObjectView {
        private final String name;
        private final MeshManager.MeshEntity mesh;

        ObjectEntity(final String name, final MeshManager.MeshEntity mesh) {
            this.name = name;
            this.mesh = mesh;
        }

        @Override
        public Vector3f getPosition() {
            return null;
        }

        @Override
        public Vector3f getRotation() {
            return null;
        }

        @Override
        public Vector3f getScale() {
            return null;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public boolean isLit() {
            return false;
        }

        @Override
        public boolean hasProgram() {
            return false;
        }

        @Override
        public DrawMode getDrawMode() {
            return null;
        }

        @Override
        public ProgramView getProgram() {
            return null;
        }

        @Override
        public void setPosition(final Vector3f position) {

        }

        @Override
        public void setRotation(final Vector3f rotation) {

        }

        @Override
        public void setScale(final Vector3f scale) {

        }

        @Override
        public void move(final Vector3f offset) {

        }

        @Override
        public void rotate(final Vector3f angles) {

        }

        @Override
        public void scale(final Vector3f factor) {

        }

        @Override
        public void setDrawMode(final DrawMode drawMode) {

        }

        @Override
        public void setLit(final boolean lit) {

        }

        @Override
        public void setVisible(final boolean visible) {

        }

        @Override
        public void setProgram(final ProgramView program) {

        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class ObjectAccessor extends AbstractAccessor {}
}
