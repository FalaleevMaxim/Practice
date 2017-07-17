package contactBook.controller;

import contactBook.dto.GroupDto;
import contactBook.dto.UserDto;
import contactBook.model.Group;
import contactBook.model.User;
import contactBook.repository.GroupRepository;
import contactBook.repository.UserRepository;
import org.postgresql.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ContactBook")
public class ContactsController {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;

    @Autowired
    public ContactsController(UserRepository userRepo, GroupRepository groupRepo) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
    }

    @RequestMapping("")
    public String main(){
        return "main";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/me")
    @ResponseBody public UserDto myProperties(){
        return new UserDto(getCurrentUser());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/contacts")
    public @ResponseBody Set<GroupDto> getContacts(){
        return groupRepo.getGroupsByOwner(getCurrentUser()).stream().map(GroupDto::new).collect(Collectors.toSet());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/addGroup/{name}")
    public @ResponseBody int addGroup(@PathVariable String name){
        Group newGroup = new Group();
        newGroup.setName(name);
        newGroup.setOwner(getCurrentUser());
        return groupRepo.saveAndFlush(newGroup).getId();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/addContact/{groupId}")
    public ResponseEntity<?> addContact(@PathVariable int groupId){
        Group group = groupRepo.findOne(groupId);
        if(group==null){
            return new ResponseEntity<>("Group not found", HttpStatus.NOT_FOUND);
        }
        if(!getCurrentUser().equals(group.getOwner())){
            return new ResponseEntity<>("You are not owner of this group", HttpStatus.FORBIDDEN);
        }
        User newContact = userRepo.saveAndFlush(new User());
        group.getMembers().add(newContact);
        groupRepo.saveAndFlush(group);
        return new ResponseEntity<>(newContact.getId(),HttpStatus.CREATED);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(method = RequestMethod.POST, value = "/update")
    public ResponseEntity<?> updateContact(@ModelAttribute UserDto userDto){
        User user = userRepo.findOne(userDto.getId());
        if(user==null) return new ResponseEntity<>("Contact or user do not exist",HttpStatus.NOT_FOUND);
        User currentUser = getCurrentUser();
        boolean isSelf = currentUser.getId()==user.getId();
        if(user.getUserName()!=null && !isSelf)
            return new ResponseEntity<>("Can not modify other user",HttpStatus.FORBIDDEN);
        boolean isInContacts = isInContacts(user);
        if(!isInContacts && !isSelf) return new ResponseEntity<>("Can not modify other user's contact",HttpStatus.FORBIDDEN);
        user.setNick(userDto.getNick());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getNumber());
        userRepo.saveAndFlush(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping("/removeGroup/{id}")
    public ResponseEntity<?> removeGroup(@PathVariable int id){
        Group group = groupRepo.findOne(id);
        if(group==null){
            return new ResponseEntity<>("Group not found", HttpStatus.NOT_FOUND);
        }
        if(!getCurrentUser().equals(group.getOwner())){
            return new ResponseEntity<>("You are not owner of this group", HttpStatus.FORBIDDEN);
        }
        Set<User> fakeUsersForDelete = new HashSet<>();
        for(User user : group.getMembers()){
            group.getMembers().remove(user);
            if(user.getUserName()==null) fakeUsersForDelete.add(user);
        }
        groupRepo.flush();
        userRepo.delete(fakeUsersForDelete);
        userRepo.flush();
        groupRepo.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping("/removeContact/{id}")
    public ResponseEntity<?> removeContact(@PathVariable int id){
        User user = userRepo.findOne(id);
        if(user==null) return new ResponseEntity<>("Contact or user do not exist",HttpStatus.NOT_FOUND);
        User currentUser = getCurrentUser();
        if(currentUser.getId()==user.getId()) return new ResponseEntity<>("You can not remove yourself",HttpStatus.FORBIDDEN);
        if(user.getUserName()!=null)
            return new ResponseEntity<>("Can not remove other user",HttpStatus.FORBIDDEN);
        boolean isInContacts = isInContacts(user);
        if(!isInContacts) return new ResponseEntity<>("Can not remove other user's contact",HttpStatus.FORBIDDEN);
        userRepo.delete(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*@Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(method = RequestMethod.POST, value = "/uploadImage/{userId}")
    @PostMapping(value = "/uploadImage/{userId}")
    public ResponseEntity<?> uploadImage(@PathVariable int userId, @RequestParam("file") MultipartFile file){
        User user = userRepo.findOne(userId);
        if(user==null) return new ResponseEntity<>("Contact or user do not exist",HttpStatus.NOT_FOUND);
        if(getCurrentUser().getId()!=userId && !isInContacts(user))
            return new ResponseEntity<>("Can not upload image for other user",HttpStatus.FORBIDDEN);
        if(file.isEmpty()) return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        try {
            byte[] bytes = file.getBytes();
            String encodedFile = Base64.encodeBytes(bytes);
            user.setAvatar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public boolean isInContacts(User user) {
        User currentUser = getCurrentUser();
        boolean isInContacts = false;
        for(Group g : currentUser.getGroups()){
            for(User u : g.getMembers()){
                if(u.getId()==user.getId()){
                    isInContacts = true;
                    break;
                }
            }
            if(isInContacts) break;
        }
        return isInContacts;
    }


    private User getCurrentUser() {
        return userRepo.getUserByUserName(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
