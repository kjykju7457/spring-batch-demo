package batch;

import batch.jobs.object.key.SelectObjectKeyJobConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SelectObjectKeyJobConfig.class})
public class SelectObjectKeyJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void 오브젝트키_추출_잡_테스트() throws Exception {

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                        .addLong("month", 4L).toJobParameters()

        );

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
