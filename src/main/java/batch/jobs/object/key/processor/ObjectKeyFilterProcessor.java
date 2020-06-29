package batch.jobs.object.key.processor;


import batch.domain.ObjectKeyInfo;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//@StepScope
@Component
public class ObjectKeyFilterProcessor implements ItemProcessor<ObjectKeyInfo, ObjectKeyInfo> {


//      @Value("#{jobParameters['month']}")
//      private Long month;

    @Override
    public ObjectKeyInfo process(ObjectKeyInfo objectKeyInfo) {
        if (objectKeyInfo.getLastModified().isBefore(LocalDateTime.now().minusMonths(6L))) {
            return null;
        }
        return objectKeyInfo;
    }
}
