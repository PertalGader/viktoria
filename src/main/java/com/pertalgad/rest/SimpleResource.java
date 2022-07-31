package com.pertalgad.rest;

import com.pertalgad.domain.Simple;
import com.pertalgad.viktoria.client.metrics.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class SimpleResource {
    private static final Logger log = LoggerFactory.getLogger(SimpleResource.class.getName());
    private final String counterName = "requests_total{method=\"GET\", path=\"/counter/\"}";
    private Counter counter;

    public SimpleResource(){
        counter = new Counter(counterName);
    }

    @GetMapping(path = "/counter/{objectsCount}", produces = "application/json")
    public ResponseEntity<List<Simple>> getSomeObjectsCounter(@PathVariable int objectsCount){
        counter.inc();

        List<Simple> simpleList = new ArrayList<>();
        for (int i = 0; i < objectsCount; i++) {
            simpleList.add(new Simple());
        }

        log.info(counter.getName()+ " " + counter.get());

        return new ResponseEntity<>(simpleList, HttpStatus.OK);
    }

    @GetMapping(path = "/counter/", produces = "application/json")
    public ResponseEntity<Long> getCounterInfo(){
        return new ResponseEntity<>(counter.get(), HttpStatus.OK);
    }
}
