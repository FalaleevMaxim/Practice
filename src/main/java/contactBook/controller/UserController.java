package contactBook.controller;

import contactBook.dto.RegisterDto;
import contactBook.model.User;
import contactBook.repository.UserRepository;
import contactBook.service.Encryptor.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/User")
public class UserController {
    private final UserRepository userRepo;
    private final PasswordEncryptor encryptor;

    @Autowired
    public UserController(UserRepository userRepo, PasswordEncryptor encryptor) {
        this.userRepo = userRepo;
        this.encryptor = encryptor;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/Login")
    public String LoginPage(){
        return "login";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/FreeName/{name}")
    public @ResponseBody boolean freeUsername(@PathVariable String name){
        return userRepo.findUserByUserName(name)==null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/Register")
    public String register(){
        return "register";
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(method = RequestMethod.POST, value = "/Register")
    public @ResponseBody int register(@ModelAttribute RegisterDto register){
        User newUser = new User(register.getUsername(), encryptor.encryptPassword(register.getPassword(), register.getUsername()));
        newUser.setNick(register.getNickname());
        return userRepo.saveAndFlush(newUser).getId();
    }
}