package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform1i;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.engine.enums.ProgramType;
import io.github.trimax.venta.engine.enums.ShaderUniform;
import io.github.trimax.venta.engine.enums.ToneMapping;
import io.github.trimax.venta.engine.exceptions.FramebufferCreationException;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The scene is rendered in linear HDR colors into a floating point framebuffer (multisampled like the window was),
 * then a full screen pass applies the exposure and the tone mapping and encodes the image to sRGB for the window.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostProcessingRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final Memory memory;

    /* The scene with multisampling, the resolved scene for the post pass and an empty vertex array for the full screen triangle */
    private int sceneFramebufferID;
    private int sceneColorID;
    private int sceneDepthID;
    private int resolvedFramebufferID;
    private int resolvedTextureID;
    private int vertexArrayID;

    private int width;
    private int height;

    /** Binds the HDR framebuffer of the window size for the scene and clears it */
    public void begin(final WindowState window) {
        try (final var stack = stackPush()) {
            final var widthBuffer = stack.mallocInt(1);
            final var heightBuffer = stack.mallocInt(1);
            glfwGetFramebufferSize(window.getInternalID(), widthBuffer, heightBuffer);

            // A minimized window has no pixels, the targets are kept until it comes back
            if (widthBuffer.get(0) > 0 && heightBuffer.get(0) > 0 && (widthBuffer.get(0) != width || heightBuffer.get(0) != height))
                createTargets(widthBuffer.get(0), heightBuffer.get(0), window.getSamples());
        }

        glBindFramebuffer(GL_FRAMEBUFFER, sceneFramebufferID);
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /** Resolves the scene and draws it tone mapped to the window, which then gets the overlays (console, text) */
    public void end(final float exposure, @NonNull final ToneMapping toneMapping) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, sceneFramebufferID);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, resolvedFramebufferID);
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // The debug overlays draw edges and leave the polygon mode as they set it
        final var program = programRegistry.get(ProgramType.Post.getProgramName());
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDisable(GL_DEPTH_TEST);
        glUseProgram(program.getInternalID());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, resolvedTextureID);
        glUniform1i(program.getUniformID(ShaderUniform.TextureScene), 0);
        glUniform1f(program.getUniformID(ShaderUniform.Exposure), exposure);
        glUniform1i(program.getUniformID(ShaderUniform.ToneMapping), toneMapping.getShaderValue());

        glBindVertexArray(getVertexArray());
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Sample counts to try for the scene framebuffer: drivers may support fewer samples for floating point colors than
     * for the window and OpenGL 4.1 can't tell the limit of a format, so the count is halved down to no multisampling.
     */
    static List<Integer> getSampleCounts(final int samples) {
        final var counts = new ArrayList<Integer>();
        for (var count = Math.max(samples, 0); count > 0; count /= 2)
            counts.add(count);

        counts.add(0);
        return counts;
    }

    private int getVertexArray() {
        if (vertexArrayID == 0)
            vertexArrayID = memory.getVertexArrays().create("Post processing triangle");

        return vertexArrayID;
    }

    private void createTargets(final int width, final int height, final int samples) {
        deleteTargets();

        final var created = getSampleCounts(samples).stream().anyMatch(count -> createSceneFramebuffer(width, height, count));
        if (!created)
            throw new FramebufferCreationException("HDR scene framebuffer is incomplete even without multisampling");

        resolvedTextureID = memory.getTextures().create("HDR resolved scene");
        glBindTexture(GL_TEXTURE_2D, resolvedTextureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        resolvedFramebufferID = memory.getFramebuffers().create("HDR resolved scene");
        glBindFramebuffer(GL_FRAMEBUFFER, resolvedFramebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, resolvedTextureID, 0);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        if (status != GL_FRAMEBUFFER_COMPLETE)
            throw new FramebufferCreationException(String.format("HDR resolved scene framebuffer is incomplete: 0x%X", status));

        this.width = width;
        this.height = height;
    }

    private boolean createSceneFramebuffer(final int width, final int height, final int samples) {
        deleteSceneFramebuffer();

        sceneColorID = createRenderbuffer("HDR scene color", samples, GL_RGBA16F, width, height);
        sceneDepthID = createRenderbuffer("HDR scene depth", samples, GL_DEPTH24_STENCIL8, width, height);
        sceneFramebufferID = memory.getFramebuffers().create("HDR scene");
        glBindFramebuffer(GL_FRAMEBUFFER, sceneFramebufferID);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, sceneColorID);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, sceneDepthID);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        if (status == GL_FRAMEBUFFER_COMPLETE) {
            log.info("HDR scene framebuffer {}x{} with {} samples", width, height, samples);
            return true;
        }

        log.warn("HDR scene framebuffer with {} samples isn't supported (0x{})", samples, Integer.toHexString(status));
        return false;
    }

    private int createRenderbuffer(final String name, final int samples, final int format, final int width, final int height) {
        final var renderbufferID = memory.getRenderbuffers().create(name);
        glBindRenderbuffer(GL_RENDERBUFFER, renderbufferID);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, format, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        return renderbufferID;
    }

    private void deleteTargets() {
        deleteSceneFramebuffer();

        if (resolvedFramebufferID == 0)
            return;

        memory.getFramebuffers().delete(resolvedFramebufferID);
        memory.getTextures().delete(resolvedTextureID);
        resolvedFramebufferID = 0;
    }

    private void deleteSceneFramebuffer() {
        if (sceneFramebufferID == 0)
            return;

        memory.getFramebuffers().delete(sceneFramebufferID);
        memory.getRenderbuffers().delete(sceneColorID);
        memory.getRenderbuffers().delete(sceneDepthID);
        sceneFramebufferID = 0;
    }
}
