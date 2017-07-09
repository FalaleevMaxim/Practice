package contactBook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/User")
public class UserController {
    @RequestMapping(method = RequestMethod.GET, value = "/Login")
    public String LoginPage(){
        return "login";
    }
}
