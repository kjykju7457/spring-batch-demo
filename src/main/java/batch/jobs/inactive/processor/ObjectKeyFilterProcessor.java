package batch.jobs.inactive.processor;


import batch.domain.ObjectKeyInfo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ObjectKeyFilterProcessor implements ItemProcessor<ObjectKeyInfo, ObjectKeyInfo> {


//    @Value("#{jobParameters['day']}")
//    private String day;

    @Override
    public ObjectKeyInfo process(ObjectKeyInfo objectKeyInfo) {

        if (objectKeyInfo.getLastModified().isBefore(LocalDateTime.now().minusYears(1L))) {
            return null;
        }
        return objectKeyInfo;
    }
}
