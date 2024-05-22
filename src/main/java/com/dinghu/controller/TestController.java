package com.dinghu.controller;

import com.dinghu.test.MetricsCounter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author 1.0
 * @author huding
 * @Date: 2024/05/22 09:57
 * @Description:
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private MeterRegistry meterRegistry;
    private Counter counter;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("common", "test");
        // 公共标签
        meterRegistry.config().commonTags(tags);
        counter = Counter.builder("metrics.request.common").register(meterRegistry);
    }

    @GetMapping("/t1")
    public String t1() {
        counter.increment();
        return "ok";
    }


    @GetMapping("/t2")
    public String t2() {
        counter.increment();
        return "ok";
    }




    @GetMapping("/testString")
    public String test() {
        return "hello word";
    }

}
