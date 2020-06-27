package batch;

import batch.jobs.inactive.SelectObjectKeyJobConfig;
import batch.repository.ObjectKeyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SelectObjectKeyJobConfig.class})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class InactiveObjectKeyInfoJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ObjectKeyRepository objectKeyRepository;

    @Test
    public void 휴면_회원_전환_테스트() throws Exception {
        Date nowDate = new Date();
        //JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addDate("nowDate", nowDate).toJobParameters());
        //JobExecution jobExecution = jobLauncherTestUtils.launchJob();

//		JobExecution jobExecution = jobLauncherTestUtils.launchJob(
//			//	new JobParametersBuilder().addDate("nowDate", nowDate).toJobParameters()
//		);
//
//		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
//		assertEquals(35, objectKeyRepository.findAll().size());
    }
}
