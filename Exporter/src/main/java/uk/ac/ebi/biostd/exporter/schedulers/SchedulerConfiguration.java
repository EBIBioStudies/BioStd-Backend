package uk.ac.ebi.biostd.exporter.schedulers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfiguration implements SchedulingConfigurer {
    private final List<CronTask> registeredTasks;

    public SchedulerConfiguration() {
        this.registeredTasks = Collections.emptyList();
    }

    @Autowired(required = false)
    public SchedulerConfiguration(List<CronTask> registeredTasks) {
        this.registeredTasks = registeredTasks;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
        taskRegistrar.setCronTasksList(registeredTasks);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(Executors.newScheduledThreadPool(5));
    }
}
