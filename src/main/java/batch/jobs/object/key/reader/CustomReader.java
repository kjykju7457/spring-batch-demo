package batch.jobs.object.key.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
public class CustomReader implements ItemReader<Integer> {

    private final int MAX_VALUE = 50000;

    private int i = 0;

    @Override
    public Integer read() {
        System.out.println("read" + i);
        if(i == MAX_VALUE){
            return null;
        } else {
            return i++;
        }

    }
}
