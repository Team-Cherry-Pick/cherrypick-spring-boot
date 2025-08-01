package com.cherrypick.backend.domain.deal.adapter.out;

import com.cherrypick.backend.domain.deal.dto.response.UrlInfoDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.InfoTaskErrorCode;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.oer.its.etsi102941.Url;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component @RequiredArgsConstructor
public class DealCrawlAdapter
{
    private final RedisTemplate<String, Object> redisTemplate;
    BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>(5);

    private record Task(
      String url,
      Future<UrlInfoDTO> future
    ){}

    public UrlInfoDTO requestUrlInfo(String url){

        // 1. URL과 Future 객체를 작업단위로 포장
        Future<UrlInfoDTO> future = new CompletableFuture<>();
        Task task = new Task(url, future);

        // 2. 작업 큐에 task를 투입, 만약 큐에 넣지 못한다면 오류
        var requestAccepted = taskQueue.offer(task);
        if(!requestAccepted) throw new BaseException(InfoTaskErrorCode.TASK_QUEUE_OVERLOADED);

        // 3. 값 읽기
        UrlInfoDTO info;
        try{
           info = future.get(10, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            throw new BaseException(InfoTaskErrorCode.TASK_RESPONSE_TIMEOUT);
        }
        catch (InterruptedException e) {
            throw new BaseException(InfoTaskErrorCode.SYSTEM_INTERRUPTED);
        }
        catch (ExecutionException e) {
            throw new BaseException(InfoTaskErrorCode.TASK_EXECUTION_ERROR);
        }

        return info;

    }




}
