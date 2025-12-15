package com.example.LAB5.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/ui")
    public String index() {
        return "redirect:/ui/index.html";
    }
}