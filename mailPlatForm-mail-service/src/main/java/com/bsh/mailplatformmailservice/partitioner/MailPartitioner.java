package com.bsh.mailplatformmailservice.partitioner;

import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MailPartitioner implements Partitioner {
    private final String channelId;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> result = new HashMap<>();
        Long rangeResult = 0L;

        if(channelId != null) {
            try {
                rangeResult = redisTemplate.opsForZSet().zCard(channelId);
            } catch(RedisException e) {
                log.info("redis error");
                return result;
            }
        }

        if(rangeResult != null && rangeResult > 0L) {

            int partitionIndex = 0;
            int calcSize;

            calcSize = Math.toIntExact(rangeResult);

            int calcGridSize = gridSize;
            int start = 0;

            for(int i=0; i<gridSize; i++) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + partitionIndex, value);

                int itemsPerPartition = calcSize / calcGridSize;

                value.putLong("start_index", start);

                if(calcSize % calcGridSize != 0) {
                    itemsPerPartition += 1;
                }

                value.putLong("end_index", start + itemsPerPartition - 1);

                calcSize -= itemsPerPartition;
                calcGridSize -= 1;

                if(calcSize <= 0) {
                    break;
                }

                start += itemsPerPartition;
                partitionIndex++;
            }
        }

        return result;
    }
}
