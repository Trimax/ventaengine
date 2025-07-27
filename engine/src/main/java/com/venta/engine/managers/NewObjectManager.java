package com.venta.engine.managers;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.DrawMode;
import com.venta.engine.model.view.MaterialView;
import com.venta.engine.model.view.NewObjectView;
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
public final class NewObjectManager extends AbstractManager<NewObjectManager.NewObjectEntity, NewObjectView> {
    @Override
    protected void destroy(final NewObjectEntity object) {
        log.info("Destroying object {} ({})", object.getID(), object.getName());
    }

    @Getter
    public static final class NewObjectEntity extends AbstractEntity implements NewObjectView {
        private final String name;
        private final ObjectManager.ObjectEntity mesh;

        NewObjectEntity(final String name, final ObjectManager.ObjectEntity mesh) {
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
        public void setPosition(Vector3f position) {

        }

        @Override
        public void setRotation(Vector3f rotation) {

        }

        @Override
        public void setScale(Vector3f scale) {

        }

        @Override
        public void move(Vector3f offset) {

        }

        @Override
        public void rotate(Vector3f angles) {

        }

        @Override
        public void scale(Vector3f factor) {

        }

        @Override
        public void setDrawMode(DrawMode drawMode) {

        }

        @Override
        public void setLit(boolean lit) {

        }

        @Override
        public void setVisible(boolean visible) {

        }

        @Override
        public void setMaterial(MaterialView material) {

        }

        @Override
        public void setProgram(ProgramView program) {

        }
    }

    @Component
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class NewObjectAccessor extends AbstractAccessor {}
}
