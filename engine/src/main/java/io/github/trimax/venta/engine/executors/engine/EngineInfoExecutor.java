package io.github.trimax.venta.engine.executors.engine;

import io.github.trimax.venta.container.annotations.Component;
import io.github.trimax.venta.container.utils.MetricUtil;
import io.github.trimax.venta.engine.console.ConsoleCommandQueue;
import io.github.trimax.venta.engine.factories.ControllerFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
@Component
@SuppressWarnings("unused")
public final class EngineInfoExecutor extends AbstractEngineExecutor {
    private EngineInfoExecutor(@NonNull final ControllerFactory factory) {
        super(factory, "info", "prints engine resource utilization");
    }

    @Override
    public void execute(final ConsoleCommandQueue.Command command) {
        getConsole().header("Metrics:");

        final var heap = MetricUtil.getHeapMemoryUsage();
        getConsole().info("        Heap memory used: %s", FileUtils.byteCountToDisplaySize(heap.getUsed()));
        getConsole().info("    Heap memory commited: %s", FileUtils.byteCountToDisplaySize(heap.getCommitted()));
        getConsole().info("         Heap memory max: %s", FileUtils.byteCountToDisplaySize(heap.getMax()));

        getConsole().info("     Runtime memory used: %s", FileUtils.byteCountToDisplaySize(MetricUtil.getUsedMemory()));
        getConsole().info("Runtime memory allocated: %s", FileUtils.byteCountToDisplaySize(MetricUtil.getAllocatedMemory()));
        getConsole().info("      Runtime memory max: %s", FileUtils.byteCountToDisplaySize(MetricUtil.getMaxMemory()));

        getConsole().info("           Threads count: %d", MetricUtil.getThreadCount());

        final var processCPU = MetricUtil.getProcessLoadCPU();
        if (processCPU >= 0)
            getConsole().info("        Process CPU load: %.2f%%", processCPU);

        final var systemCPU = MetricUtil.getSystemLoadCPU();
        if (systemCPU >= 0)
            getConsole().info("         System CPU load: %.2f%%", systemCPU);
    }
}
