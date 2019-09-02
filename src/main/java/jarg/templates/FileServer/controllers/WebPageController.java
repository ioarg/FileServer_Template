package jarg.templates.FileServer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String getHomepage(){
        return "home";
    }

    @GetMapping("/downloads")
    public String getDownloadsPage(){
        return "download_page";
    }
}
