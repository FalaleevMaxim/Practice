package contactBook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ContactBook")
public class ApplicationController {
    @RequestMapping("")
    public String main(){
        return "main";
    }
}
