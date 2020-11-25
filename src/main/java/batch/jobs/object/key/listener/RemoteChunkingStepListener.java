package batch.jobs.object.key.listener;

import batch.jobs.object.key.reader.CustomReader;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import javax.batch.api.listener.StepListener;

@Component
public class RemoteChunkingStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if(CustomReader.i == CustomReader.MAX_VALUE) {
            System.out.println("step finished");
            return new ExitStatus("FINISHED");
        } else {
            System.out.println("step finished");
            return new ExitStatus("CONTINUE");
        }
    }
}
