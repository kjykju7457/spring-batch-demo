package batch.jobs.inactive;


import batch.domain.ObjectKeyInfo;
import org.springframework.batch.item.ItemProcessor;

public class InactiveItemProcessor implements ItemProcessor<ObjectKeyInfo, ObjectKeyInfo> {

    @Override
    public ObjectKeyInfo process(ObjectKeyInfo objectKeyInfo) {

        return objectKeyInfo.setInactive();
    }
}
