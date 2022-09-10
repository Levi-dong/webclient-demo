package com.excel.controller;

import com.excel.service.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rpc")
public class RpcController {

    @Autowired
    RpcService rpcService;

    @GetMapping("/get/{num}")
    public Integer getNum(@PathVariable("num") int num){
        return rpcService.getNum(num);
    }

    @GetMapping("/send/webclient4/{cycle}")
    public String sendWebClient4(@PathVariable("cycle") int cycle){
        return rpcService.sendWebClient4(cycle);
    }

}
