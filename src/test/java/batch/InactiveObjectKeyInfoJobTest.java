package batch;

import batch.domain.enums.UserStatus;
import batch.jobs.inactive.InactiveUserJobConfig;
import batch.repository.ObjectKeyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={InactiveUserJobConfig.class})
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

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(
			//	new JobParametersBuilder().addDate("nowDate", nowDate).toJobParameters()
		);

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals(35, objectKeyRepository.findAll().size());
	}
}
