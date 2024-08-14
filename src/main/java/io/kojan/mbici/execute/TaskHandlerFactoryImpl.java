package io.kojan.mbici.execute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import io.kojan.mbici.tasks.CheckoutTaskHandler;
import io.kojan.mbici.tasks.GatherTaskHandler;
import io.kojan.mbici.tasks.RepoTaskHandler;
import io.kojan.mbici.tasks.RpmTaskHandler;
import io.kojan.mbici.tasks.SrpmTaskHandler;
import io.kojan.mbici.tasks.ValidateTaskHandler;
import io.kojan.workflow.TaskHandler;
import io.kojan.workflow.TaskHandlerFactory;
import io.kojan.workflow.model.Task;

class TaskHandlerFactoryImpl implements TaskHandlerFactory {
    private final Map<String, Function<Task, ? extends TaskHandler>> registry = new LinkedHashMap<>();

    private void registerHandler(Class<? extends TaskHandler> cls, Function<Task, ? extends TaskHandler> ctor) {
        registry.put(cls.getCanonicalName(), ctor);
    }

    public TaskHandlerFactoryImpl() {
        registerHandler(CheckoutTaskHandler.class, CheckoutTaskHandler::new);
        registerHandler(GatherTaskHandler.class, GatherTaskHandler::new);
        registerHandler(RepoTaskHandler.class, RepoTaskHandler::new);
        registerHandler(RpmTaskHandler.class, RpmTaskHandler::new);
        registerHandler(SrpmTaskHandler.class, SrpmTaskHandler::new);
        registerHandler(ValidateTaskHandler.class, ValidateTaskHandler::new);
    }

    @Override
    public TaskHandler createTaskHandler(Task task) {
        Function<Task, ? extends TaskHandler> ctor = registry.get(task.getHandler());
        if (ctor == null) {
            throw new IllegalArgumentException("Unsupported task handler: " + task.getHandler());
        }
        return ctor.apply(task);
    }
}
