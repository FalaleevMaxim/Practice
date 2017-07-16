package contactBook.controller;

import contactBook.model.User;
import contactBook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/User")
public class UserController {
    private final UserRepository userRepo;

    @Autowired
    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/Login")
    public String LoginPage(){
        return "login";
    }

    @RequestMapping("/{id}")
    public @ResponseBody User getUser(@PathVariable int id){
        return userRepo.findOne(id);
    }
}
