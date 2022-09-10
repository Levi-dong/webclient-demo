package com.excel.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RpcService {

    private static final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8082")
            .build();

    public String sendWebClient4(int cycle) {
        List<Integer> list = Lists.newArrayList();
        Random random = new Random();
        for (int i = 0; i < cycle; i++) {
            list.add(random.nextInt(10));
        }
        List<List<Integer>> partition = Lists.partition(list, 1000);

        List<Integer> result = Lists.newArrayList();
        //List<Integer> result = Lists.newCopyOnWriteArrayList();
        Instant start = Instant.now();

        LongAdder longAdder = new LongAdder();

        Flux.fromIterable(partition)
                .buffer(2)
                .flatMap(element -> {
                    List<Mono<Integer>> monoList = Lists.newArrayList();
                    for (List<Integer> ele : element) {
                        for (Integer num : ele) {
                            Mono<Integer> mono = webClient
                                    .get()
                                    .uri("/rpc/get/{id}", num)
                                    .retrieve()
                                    .bodyToMono(Integer.class)
                                    /*.doOnSuccess(res -> {
                                        result.add(res);
                                        //log.info("查询完成：{}", res);
                                    })*/
                                    //.doOnError(ex -> log.error("查询异常", ex))
                                    //.doOnNext(res -> result.add(res));
                                    //.doFinally(res -> MDC.clear());
                                    .log()
                                    ;
                            monoList.add(mono);
                        }
                    }
                    return Mono.just(monoList);
                })
                .subscribe(element -> {
                    longAdder.increment();
                    log.info("第{}轮执行完毕，数目{}", longAdder.intValue(), element.size());
                    //Mono.when(element).block();
                    List<Integer> block = Mono.zip(element, objectArray -> Arrays.stream(objectArray)
                            .map(Integer.class::cast)
                            .collect(Collectors.toList())).block();
                    result.addAll(block);
                });
        log.info("获取到数目：{}", result.size());
        Instant end = Instant.now();
        long ms = Duration.between(start, end).toMillis();
        log.info("耗时：{}", ms);

        return String.valueOf(ms);
    }

    public Integer getNum(int num) {
        int sleep = 1;
        try {
            int i = new Random().nextInt(10);
            sleep = num * i;
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sleep;
    }

}
