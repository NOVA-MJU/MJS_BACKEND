package nova.mjs.domain.realtimeKeyword;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RealtimeKeywordService {

    @Qualifier("keywordRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private static final String ZSET_KEY = "realtime_keywords";
    private static final String LIST_KEY_PREFIX = "search:history";

    private static final long ttl = 3L * 24 * 60 * 60 * 1000;

    //검색어 등록 : 점수 +1 & time stamp 저장
    public void recordSearch(String keyword) {
        long now = System.currentTimeMillis();

        //ZSET 점수 증가
        redisTemplate.opsForZSet().incrementScore(ZSET_KEY, keyword, 1.0);

        //time stamp 리스트에 추가
        redisTemplate.opsForList().rightPush(LIST_KEY_PREFIX + keyword, String.valueOf(now));
    }

    //상위 n개의 실시간 검색어 조회
    public List<String> getTopKeywords(int topN){
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(ZSET_KEY, 0, topN-1);

        return keywords != null ? new ArrayList<>(keywords) : Collections.emptyList();
    }

    //오래된 검색 기록 제거 및 점수 감소
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void expiredSearchRecords(){
        long now = System.currentTimeMillis();

        Set<String> keywords = redisTemplate.opsForZSet().range(ZSET_KEY, 0, -1);

        if(keywords == null || keywords.isEmpty()) return;

        for(String keyword : keywords){
            String historyKey = LIST_KEY_PREFIX + keyword;

            List<String> timestamps = redisTemplate.opsForList().range(historyKey, 0, -1);

            if(timestamps == null || timestamps.isEmpty()) continue;

            boolean changed = false;

            for(String ts : new ArrayList<>(timestamps)){
                long time = Long.parseLong(ts);
                if(now - time > ttl){
                    //오래된 timestamp 제거
                    redisTemplate.opsForList().remove(historyKey, 1, ts);

                    //점수 감소
                    redisTemplate.opsForZSet().incrementScore(ZSET_KEY, keyword, -1.0);
                    changed = true;
                }
            }

            Double score = redisTemplate.opsForZSet().score(ZSET_KEY, keyword);
            if(score != null && score <= 0){
                redisTemplate.opsForZSet().remove(ZSET_KEY, keyword);
                redisTemplate.delete(historyKey);
            }
        }
    }
}