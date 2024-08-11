package ru.sm.lab.demo.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.sm.lab.demo.config.JobConfig;

import java.util.Properties;

@ShellComponent
@RequiredArgsConstructor
public class JobCommands {

    private final JobOperator jobOperator;

    @ShellMethod(value = "Start data migration", key = "sdm")
    public String startDataMigrationJob() throws Exception {
        Properties jobParameters = new Properties();
        jobParameters.put("Time", String.valueOf(System.currentTimeMillis()));
        jobOperator.start(JobConfig.JOB_NAME, jobParameters);
        return "The migration has been completed";
    }
}
