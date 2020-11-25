package batch.jobs.object.key.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
public class CustomReader implements ItemReader<Integer> {

    public static final int MAX_VALUE = 100;

    public static int i = 0;

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
