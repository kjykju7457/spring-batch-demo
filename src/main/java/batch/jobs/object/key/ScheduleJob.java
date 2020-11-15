package batch.jobs.object.key;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleJob {

    //@Value("${jobs.enabled}")
    private boolean isEnabled = true;

    private final Job selectObjectKeyJob;

    private final JobLauncher jobLauncher;

    @Scheduled(fixedRate = 10_000)
    public void launchJob() throws Exception {
        if (isEnabled) {
            log.debug("scheduler starts at {}" , LocalDateTime.now());
            JobExecution jobExecution = jobLauncher.run(selectObjectKeyJob, new JobParametersBuilder().addLong("month", 7L)
                    .toJobParameters());
            log.debug("Batch job ends with status as " + jobExecution.getStatus());
            log.debug("scheduler ends ");
        }

    }
}
