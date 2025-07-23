package com.venta.engine.renderers;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

import com.venta.engine.annotations.Component;
import com.venta.engine.enums.TextureType;
import com.venta.engine.exceptions.ObjectRenderingException;
import com.venta.engine.managers.ObjectManager;
import com.venta.engine.model.view.ObjectView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class ObjectRenderer extends AbstractRenderer<ObjectManager.ObjectEntity, ObjectView> {
    @Override
    public void render(final ObjectView object) {
        final var programView = object.getProgram();
        if (programView != null) {
            final var context = getContext();
            if (context == null)
                throw new ObjectRenderingException("RenderContext is not set. Did you forget to call withContext()?");

            glUseProgram(programView.entity.getIdAsInteger());

            glUniform4f(programView.entity.getUniformID("ambientLight"),
                    context.getAmbientLight().x(),
                    context.getAmbientLight().y(),
                    context.getAmbientLight().z(),
                    context.getAmbientLight().w());

            final var position = object.getPosition();
            glUniform3f(programView.entity.getUniformID("translation"), position.x(), position.y(), position.z());

            final var rotation = object.getRotation();
            glUniform3f(programView.entity.getUniformID("rotation"), rotation.x(), rotation.y(), rotation.z());

            final var scale = object.getScale();
            glUniform3f(programView.entity.getUniformID("scale"), scale.x(), scale.y(), scale.z());

            glUniformMatrix4fv(programView.entity.getUniformID("view"), false, context.getViewMatrixBuffer());
            glUniformMatrix4fv(programView.entity.getUniformID("projection"), false, context.getProjectionMatrixBuffer());

            final var material = object.getMaterial();
            if (material != null) {
                final var textureDiffuse = material.getTexture(TextureType.DIFFUSE);
                glUniform1i(programView.entity.getUniformID("useTextureDiffuse"), textureDiffuse == null ? 0 : 1);
                if (textureDiffuse != null) {
                    glActiveTexture(GL_TEXTURE0);

                    glBindTexture(GL_TEXTURE_2D, textureDiffuse.entity.getIdAsInteger());
                    glUniform1i(programView.entity.getUniformID("textureDiffuse"), 0);
                }

                final var textureHeight = material.getTexture(TextureType.HEIGHT);
                glUniform1i(programView.entity.getUniformID("useTextureHeight"), textureHeight == null ? 0 : 1);
                if (textureHeight != null) {
                    glActiveTexture(GL_TEXTURE1);

                    glBindTexture(GL_TEXTURE_2D, textureHeight.entity.getIdAsInteger());
                    glUniform1i(programView.entity.getUniformID("textureHeight"), 0);
                }
            }

            final var lights = context.getLights();
            glUniform1i(programView.entity.getUniformID("lightCount"), lights.size());

            for (int i = 0; i < lights.size(); i++) {
                final var light = lights.get(i);

                final var prefix = "lights[" + i + "]";
                glUniform1i(programView.entity.getUniformID(prefix + ".type"), 0); //TODO: Only point light supported so far
                glUniform3f(programView.entity.getUniformID(prefix + ".position"),
                        light.getPosition().x(), light.getPosition().y(), light.getPosition().z());
                glUniform3f(programView.entity.getUniformID(prefix + ".direction"),
                        light.getDirection().x(), light.getDirection().y(), light.getDirection().z());
                glUniform3f(programView.entity.getUniformID(prefix + ".color"),
                        light.getColor().x(), light.getColor().y(), light.getColor().z());
                glUniform1f(programView.entity.getUniformID(prefix + ".intensity"), 1.0f); //TODO: Pass from view
                glUniform1i(programView.entity.getUniformID(prefix + ".castShadows"), 0); //TODO: Pass from view
                glUniform1i(programView.entity.getUniformID(prefix + ".enabled"), 1);

                final var attenuation = light.getAttenuation();
                glUniform1f(programView.entity.getUniformID(prefix + ".attenuation.constant"), attenuation.constant());
                glUniform1f(programView.entity.getUniformID(prefix + ".attenuation.linear"), attenuation.linear());
                glUniform1f(programView.entity.getUniformID(prefix + ".attenuation.quadratic"), attenuation.quadratic());
            }
        }

        glBindVertexArray(object.entity.getVertexArrayObjectID());
        glDrawElements(GL_TRIANGLES, object.entity.getFacets().length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
