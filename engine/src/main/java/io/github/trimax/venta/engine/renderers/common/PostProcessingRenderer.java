package io.github.trimax.venta.engine.renderers.common;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE1;
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
import io.github.trimax.venta.engine.helpers.BloomHelper;
import io.github.trimax.venta.engine.helpers.FxaaHelper;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.common.shared.Antialiasing;
import io.github.trimax.venta.engine.model.common.shared.Bloom;
import io.github.trimax.venta.engine.model.states.WindowState;
import io.github.trimax.venta.engine.registries.implementation.ProgramRegistryImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The scene is rendered in linear HDR colors into a floating point framebuffer (multisampled like the window was),
 * then a full screen pass applies the exposure and the tone mapping and encodes the image to sRGB for the window. With
 * an antialiasing the pass draws into a texture of the colors of the window instead, and FXAA smooths it into the window.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostProcessingRenderer {
    private final ProgramRegistryImplementation programRegistry;
    private final Memory memory;

    /* The scene with multisampling, the resolved scene for the post pass and an empty vertex array for the full screen triangle */
    private int sceneFramebufferID;

    /** The samples of the scene framebuffer that was created: it can have fewer than the window asks for, or none */
    private int sceneSamples;
    private int sceneColorID;
    private int sceneDepthID;
    private int resolvedFramebufferID;

    /**
     * The color of the resolved scene: the post passes read it, and so does the water in the middle of the frame
     * (see {@link #resolveScene()}).
     */
    @Getter
    private int resolvedTextureID;

    /* The depth of the resolved scene, and a framebuffer of its color alone: a pass can add light to the scene and read the depth */
    @Getter
    private int resolvedDepthTextureID;
    @Getter
    private int resolvedColorFramebufferID;
    private int vertexArrayID;

    /** The size of the scene in pixels */
    @Getter
    private int width;

    @Getter
    private int height;

    /** The tone mapped image that FXAA reads, created with the first frame that has an antialiasing */
    private int displayFramebufferID;
    private int displayTextureID;

    /** Makes the glow of the bright parts, created with the first frame that has a bloom */
    private BloomHelper bloomHelper;

    /** Smooths the edges of the tone mapped image, created with the first frame that has an antialiasing */
    private FxaaHelper fxaaHelper;

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

    /**
     * Resolves the multisampled scene into the textures the post passes read: the color and, when a pass needs it, the depth.
     *
     * @param depth whether the depth is resolved as well (see {@link #getResolvedDepthTextureID()})
     */
    public void resolve(final boolean depth) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, sceneFramebufferID);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, resolvedFramebufferID);
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, depth ? GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT : GL_COLOR_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Resolves what is drawn so far (the color and the depth) in the middle of the frame, and goes on drawing into the scene: a
     * surface drawn after it (the water) can read the opaque scene behind it. The post passes resolve it again after the frame.
     */
    public void resolveScene() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, sceneFramebufferID);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, resolvedFramebufferID);
        glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST);
        glBindFramebuffer(GL_FRAMEBUFFER, sceneFramebufferID);
    }

    /**
     * Draws the resolved scene tone mapped to the window, which then gets the overlays (console, text).
     * The scene must be resolved (see {@link #resolve(boolean)}), the passes that add light to it have been made.
     *
     * @param bloom        the glow of the bright parts that is added to the scene, null or with no intensity for none
     * @param antialiasing the smoothing of the edges of the finished image, null or switched off for none
     */
    public void end(final float exposure, @NonNull final ToneMapping toneMapping, final Bloom bloom, final Antialiasing antialiasing) {
        final var glow = bloom != null && bloom.getIntensity() > 0.f ? getBloomHelper().render(resolvedTextureID, width, height, bloom) : 0;
        final var smoothed = antialiasing != null && antialiasing.isActive();

        glBindFramebuffer(GL_FRAMEBUFFER, smoothed ? getDisplayFramebuffer() : 0);
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
        glUniform1i(program.getUniformID(ShaderUniform.UseBloom), glow != 0 ? 1 : 0);
        if (glow != 0) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, glow);
            glUniform1i(program.getUniformID(ShaderUniform.TextureBloom), 1);
            glUniform1f(program.getUniformID(ShaderUniform.BloomIntensity), bloom.getIntensity());
            glActiveTexture(GL_TEXTURE0);
        }

        glUniform1f(program.getUniformID(ShaderUniform.Exposure), exposure);
        glUniform1i(program.getUniformID(ShaderUniform.ToneMapping), toneMapping.getShaderValue());

        glBindVertexArray(getVertexArray());
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0);
        glUseProgram(0);

        if (smoothed) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            getFxaaHelper().render(displayTextureID, width, height, antialiasing);
        }

        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Releases the framebuffers, the texture and the vertex array: they belong to the renderer and are deleted before
     * the memory of the engine is cleaned up, which reports everything that is still there. The next frame creates
     * them again.
     */
    public void release() {
        deleteTargets();

        if (bloomHelper != null) {
            bloomHelper.release();
            bloomHelper = null;
        }

        if (fxaaHelper != null) {
            fxaaHelper.release();
            fxaaHelper = null;
        }

        if (vertexArrayID != 0) {
            memory.getVertexArrays().delete(vertexArrayID);
            vertexArrayID = 0;
        }

        width = 0;
        height = 0;
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

    private BloomHelper getBloomHelper() {
        if (bloomHelper == null)
            bloomHelper = new BloomHelper(memory, name -> programRegistry.get(name).getInternalID());

        return bloomHelper;
    }

    private FxaaHelper getFxaaHelper() {
        if (fxaaHelper == null)
            fxaaHelper = new FxaaHelper(memory, name -> programRegistry.get(name).getInternalID());

        return fxaaHelper;
    }

    /** The framebuffer of the tone mapped image, the size of the scene */
    private int getDisplayFramebuffer() {
        if (displayFramebufferID != 0)
            return displayFramebufferID;

        displayTextureID = memory.getTextures().create("Tone mapped scene");
        glBindTexture(GL_TEXTURE_2D, displayTextureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        displayFramebufferID = memory.getFramebuffers().create("Tone mapped scene");
        glBindFramebuffer(GL_FRAMEBUFFER, displayFramebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, displayTextureID, 0);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        if (status != GL_FRAMEBUFFER_COMPLETE)
            throw new FramebufferCreationException(String.format("Tone mapped scene framebuffer is incomplete: 0x%X", status));

        return displayFramebufferID;
    }

    /** Whether the scene framebuffer has more than one sample per pixel: only then there is something to smooth by alpha to coverage */
    public boolean isMultisampled() {
        return sceneSamples > 1;
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

        // The depth is a texture of the format of the depth of the scene: a blit between buffers needs the same formats
        resolvedDepthTextureID = memory.getTextures().create("HDR resolved scene depth");
        glBindTexture(GL_TEXTURE_2D, resolvedDepthTextureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, width, height, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        resolvedFramebufferID = memory.getFramebuffers().create("HDR resolved scene");
        glBindFramebuffer(GL_FRAMEBUFFER, resolvedFramebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, resolvedTextureID, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, resolvedDepthTextureID, 0);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            throw new FramebufferCreationException(String.format("HDR resolved scene framebuffer is incomplete: 0x%X", status));
        }

        // A pass that reads the depth can't draw into a framebuffer that has it attached, so the color has a framebuffer of its own
        resolvedColorFramebufferID = memory.getFramebuffers().create("HDR resolved scene color");
        glBindFramebuffer(GL_FRAMEBUFFER, resolvedColorFramebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, resolvedTextureID, 0);
        final var colorStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        if (colorStatus != GL_FRAMEBUFFER_COMPLETE)
            throw new FramebufferCreationException(String.format("HDR resolved scene color framebuffer is incomplete: 0x%X", colorStatus));

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
            sceneSamples = samples;
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
        deleteDisplayFramebuffer();

        if (resolvedFramebufferID == 0)
            return;

        memory.getFramebuffers().delete(resolvedFramebufferID);
        memory.getFramebuffers().delete(resolvedColorFramebufferID);
        memory.getTextures().delete(resolvedTextureID);
        memory.getTextures().delete(resolvedDepthTextureID);
        resolvedFramebufferID = 0;
        resolvedColorFramebufferID = 0;
    }

    private void deleteDisplayFramebuffer() {
        if (displayFramebufferID == 0)
            return;

        memory.getFramebuffers().delete(displayFramebufferID);
        memory.getTextures().delete(displayTextureID);
        displayFramebufferID = 0;
        displayTextureID = 0;
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
