package io.github.trimax.venta.engine.model.common.reflection;

import java.util.IdentityHashMap;
import java.util.Map;

import io.github.trimax.venta.engine.model.instance.implementation.ObjectInstanceImplementation;

/**
 * The reflection probes of the objects that have them now: the scene draws the objects with their probes that are ready.
 */
public final class ReflectionProbes {
    private final Map<ObjectInstanceImplementation, ReflectionProbe> probes = new IdentityHashMap<>();

    /** The probe of an object, null when it has none or it is not ready (all its faces were never drawn) */
    public ReflectionProbe getReady(final ObjectInstanceImplementation object) {
        final var probe = probes.get(object);
        return probe != null && probe.isReady() ? probe : null;
    }

    public ReflectionProbe get(final ObjectInstanceImplementation object) {
        return probes.get(object);
    }

    public void put(final ReflectionProbe probe) {
        probes.put(probe.getObject(), probe);
    }

    /** Takes the probe of an object away and releases it */
    public void remove(final ObjectInstanceImplementation object) {
        final var probe = probes.remove(object);
        if (probe != null)
            probe.release();
    }

    /** Releases the probes of the objects that don't pass the filter */
    public void retain(final java.util.function.Predicate<ObjectInstanceImplementation> keep) {
        probes.values().removeIf(probe -> {
            if (keep.test(probe.getObject()))
                return false;

            probe.release();
            return true;
        });
    }

    public Iterable<ReflectionProbe> all() {
        return probes.values();
    }

    public int size() {
        return probes.size();
    }

    public void clear() {
        probes.values().forEach(ReflectionProbe::release);
        probes.clear();
    }
}
