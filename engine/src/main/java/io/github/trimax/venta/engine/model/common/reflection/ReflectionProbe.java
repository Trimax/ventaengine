package io.github.trimax.venta.engine.model.common.reflection;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30C.*;

import java.nio.ByteBuffer;

import io.github.trimax.venta.engine.exceptions.FramebufferCreationException;
import io.github.trimax.venta.engine.memory.Memory;
import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;
import io.github.trimax.venta.engine.utils.CubeFaceUtil;
import lombok.Getter;
import lombok.NonNull;

/**
 * The cube map of the surroundings of an object: the scene seen from the position of the object, in HDR colors, with the mip levels
 * (the blurrier a reflection is, the higher the level it reads). The six faces are drawn one by one, in different frames.
 */
public final class ReflectionProbe {
    private final Memory memory;

    @Getter
    private final ObjectInstanceImplementation object;

    @Getter
    private final ReflectionSettings settings;

    @Getter
    private final int textureID;

    @Getter
    private final int framebufferID;

    private final int depthID;

    /** The number of the mip levels of the cube map */
    @Getter
    private final int levels;

    /** How many faces of the drawing that is going on are left to draw, 0 when there is none */
    @Getter
    private int facesLeft;

    /** The seconds since the last drawing started */
    private float age;

    /** Whether all the faces were drawn at least once: before it the cube map has holes, so the object doesn't use it */
    @Getter
    private boolean ready;

    /** The frame of the last face: the probe that waits the longest goes first */
    @Getter
    private long lastFrame = -1;

    public ReflectionProbe(@NonNull final Memory memory, @NonNull final ObjectInstanceImplementation object, @NonNull final ReflectionSettings settings) {
        this.memory = memory;
        this.object = object;
        this.settings = settings;
        this.levels = 32 - Integer.numberOfLeadingZeros(settings.getResolution());

        final var resolution = settings.getResolution();
        textureID = memory.getTextures().create("Reflection probe");
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        for (int face = 0; face < CubeFaceUtil.FACES; face++)
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, GL_RGBA16F, resolution, resolution, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        depthID = memory.getRenderbuffers().create("Reflection probe depth");
        glBindRenderbuffer(GL_RENDERBUFFER, depthID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, resolution, resolution);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        framebufferID = memory.getFramebuffers().create("Reflection probe");
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X, textureID, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthID);
        final var status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            release();
            throw new FramebufferCreationException(String.format("The framebuffer of a reflection probe is incomplete: 0x%X", status));
        }
    }

    /** Time passes: a drawing starts when the probe has none and is due (or was never drawn) */
    public void advance(final float delta) {
        age += delta;

        if (facesLeft == 0 && (!ready || (!settings.isStatic() && age >= settings.getRefresh()))) {
            facesLeft = CubeFaceUtil.FACES;
            age = 0.f;
        }
    }

    /** Whether a face is to be drawn */
    public boolean isDrawing() {
        return facesLeft > 0;
    }

    /** The face that is drawn next, from 0 to 5 */
    public int getNextFace() {
        return CubeFaceUtil.FACES - facesLeft;
    }

    /** The next face is drawn: the framebuffer shows the face and the mip levels are made again */
    public void bindFace(final int face) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, textureID, 0);
        glViewport(0, 0, settings.getResolution(), settings.getResolution());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /** The face is drawn */
    public void finishFace(final long frame) {
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        lastFrame = frame;
        facesLeft--;
        if (facesLeft == 0)
            ready = true;
    }

    /** Deletes the cube map, the depth and the framebuffer */
    public void release() {
        memory.getFramebuffers().delete(framebufferID);
        memory.getRenderbuffers().delete(depthID);
        memory.getTextures().delete(textureID);
    }
}
