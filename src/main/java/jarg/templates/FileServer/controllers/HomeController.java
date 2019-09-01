/*
* A controller to simply serve the home page
 */
package jarg.templates.FileServer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getHomepage(){
        return "home";
    }
}
