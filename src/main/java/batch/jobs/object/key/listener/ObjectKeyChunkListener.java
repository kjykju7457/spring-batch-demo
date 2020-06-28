package batch.jobs.object.key.listener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ObjectKeyChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        log.info("Before Chunk");
    }

    @SneakyThrows
    @Override
    public void afterChunk(ChunkContext context) {
        log.info("After Chunk");
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        log.info("After Chunk Error");
    }
}
