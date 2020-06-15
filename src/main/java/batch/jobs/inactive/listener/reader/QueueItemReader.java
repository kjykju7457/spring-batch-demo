package batch.jobs.inactive.listener.reader;

import org.springframework.batch.item.ItemReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class QueueItemReader<T> implements ItemReader<T> {

    private Queue<T> queue;

    public QueueItemReader(List<T> data) {
        this.queue = new LinkedList<>();
    }

    @Override
    public T read() {
        return this.queue.poll();
    }
}
