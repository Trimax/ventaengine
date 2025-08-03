package io.github.trimax.venta.container.utils;

import com.sun.management.OperatingSystemMXBean;
import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

@UtilityClass
public final class MetricUtil {
    private static final OperatingSystemMXBean osBean = getOperatingSystemMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final Runtime runtime = Runtime.getRuntime();

    public MemoryUsage getHeapMemoryUsage() {
        return memoryBean.getHeapMemoryUsage();
    }

    public long getUsedMemory() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public long getAllocatedMemory() {
        return runtime.totalMemory();
    }

    public long getMaxMemory() {
        return runtime.maxMemory();
    }

    public int getThreadCount() {
        return threadBean.getThreadCount();
    }

    public double getProcessLoadCPU() {
        if (osBean != null)
            return osBean.getProcessCpuLoad() * 100.0;

        return -1;
    }

    public static double getSystemLoadCPU() {
        if (osBean != null)
            return osBean.getCpuLoad() * 100.0;

        return -1;
    }

    private OperatingSystemMXBean getOperatingSystemMXBean() {
        final java.lang.management.OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
        if (mxBean instanceof OperatingSystemMXBean bean)
            return bean;

        return null;
    }
}
