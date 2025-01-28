package com.pos.report.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JenisItemController {

    @GetMapping("/testing")
    public String hello() {
        return "Hello";
    }
}
