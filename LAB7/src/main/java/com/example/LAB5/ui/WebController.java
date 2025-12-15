package com.example.LAB5.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String redirectToUi() {
        return "redirect:/ui/index.html";
    }
}