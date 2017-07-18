package contactBook.controller;

import contactBook.dto.FieldValueDto;
import contactBook.dto.GroupDto;
import contactBook.dto.UserDto;
import contactBook.model.*;
import contactBook.repository.*;
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
import java.net.Proxy;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ContactBook")
public class ContactsController {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final PictureRepository pictureRepo;
    private final FieldRepository fieldRepo;
    private final FieldValueRepository fieldValueRepo;

    @Autowired
    public ContactsController(UserRepository userRepo, GroupRepository groupRepo, PictureRepository pictureRepo,
                              FieldRepository fieldRepo, FieldValueRepository fieldValueRepo) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.pictureRepo = pictureRepo;
        this.fieldRepo = fieldRepo;
        this.fieldValueRepo = fieldValueRepo;
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
    public ResponseEntity<?> updateContact(@RequestBody UserDto userDto){
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
        user.getFields().clear();
        for(FieldValueDto fv : userDto.getFieldValues()){
            Optional<FieldValue> fieldValue = user.getFields().stream().filter(fieldValue1 -> fieldValue1.getId()==fv.getId()).findFirst();
            if(fieldValue.isPresent()){
                fieldValue.get().setId(fv.getId());
                fieldValue.get().setField(fieldRepo.findOne(fv.getFieldId()));
                fieldValue.get().setUser(userRepo.findOne(fv.getUserId()));
                fieldValue.get().setValue(fv.getValue());
            }
        }
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
            if(user.getUserName()==null) fakeUsersForDelete.add(user);
        }
        group.getMembers().clear();
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

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(method = RequestMethod.POST, value = "/uploadImage/{userId}")
    public ResponseEntity<?> uploadImage(@PathVariable int userId, @RequestParam("file") MultipartFile file){
        User user = userRepo.findOne(userId);
        if(user==null) return new ResponseEntity<>("Contact or user do not exist",HttpStatus.NOT_FOUND);
        if(getCurrentUser().getId()!=userId && !isInContacts(user))
            return new ResponseEntity<>("Can not upload image for other user",HttpStatus.FORBIDDEN);
        if(file.isEmpty()) return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        try {
            byte[] bytes = file.getBytes();
            String encodedFile = Base64.encodeBytes(bytes);
            Picture picture = new Picture();
            picture.setUserId(userId);
            picture.setData(encodedFile);
            pictureRepo.saveAndFlush(picture);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Failed to load image",HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/fieldId/{fieldName}")
    public @ResponseBody int fieldId(@PathVariable String fieldName){
        Field field = fieldRepo.findFieldByName(fieldName);
        if(field!=null) return field.getId();
        return createField(fieldName);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private @ResponseBody int createField(String fieldName){
        return fieldRepo.saveAndFlush(new Field(fieldName)).getId();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/setFieldValue/{userId}/{fieldId}")
    public ResponseEntity<?> addFieldToUser(@PathVariable int userId, @PathVariable int fieldId,
                                            @RequestParam(value = "value",defaultValue = "") String value){
        User user = userRepo.findOne(userId);
        if(user==null) return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        if(!isInContacts(user) && user.getId()!=getCurrentUser().getId())
            return new ResponseEntity<>("User is not in contacts", HttpStatus.FORBIDDEN);
        Field field = fieldRepo.findOne(fieldId);
        if(field==null) return new ResponseEntity<>("Field not found", HttpStatus.NOT_FOUND);
        FieldValue fieldValue = fieldValueRepo.findByUserAndField(user,field);
        if(fieldValue==null) fieldValue = new FieldValue(user,field);
        fieldValue.setValue(value);
        return new ResponseEntity<>(fieldValueRepo.saveAndFlush(fieldValue).getId(),HttpStatus.OK);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping("/removeFieldValue/{id}")
    public ResponseEntity<?> removeFieldValueById(@PathVariable int id){
        FieldValue fieldValue = fieldValueRepo.findOne(id);
        if(fieldValue==null) return new ResponseEntity<>("Field value not found", HttpStatus.NOT_FOUND);
        if(!isInContacts(fieldValue.getUser()) && fieldValue.getUser().getId()!=getCurrentUser().getId())
            return new ResponseEntity<>("User is not in contacts", HttpStatus.FORBIDDEN);
        fieldValueRepo.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping("/setFieldValueById/{id}/{value}")
    public ResponseEntity<?> removeFieldValueById(@PathVariable int id, @PathVariable String value){
        FieldValue fieldValue = fieldValueRepo.findOne(id);
        if(fieldValue==null) return new ResponseEntity<>("Field value not found", HttpStatus.NOT_FOUND);
        if(!isInContacts(fieldValue.getUser()) && fieldValue.getUser().getId()!=getCurrentUser().getId())
            return new ResponseEntity<>("User is not in contacts", HttpStatus.FORBIDDEN);
        fieldValue.setValue(value);
        return new ResponseEntity<>(fieldValueRepo.saveAndFlush(fieldValue), HttpStatus.OK);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/removeFieldValue/{userId}/{fieldId}")
    public ResponseEntity<?> removeFieldValue(@PathVariable int userId, @PathVariable int fieldId){
        User user = userRepo.findOne(userId);
        if(user==null) return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        if(!isInContacts(user) && user.getId()!=getCurrentUser().getId())
            return new ResponseEntity<>("User is not in contacts", HttpStatus.FORBIDDEN);
        Field field = fieldRepo.findOne(fieldId);
        if(field==null) return new ResponseEntity<>("Field not found", HttpStatus.NOT_FOUND);
        FieldValue fieldValue = fieldValueRepo.findByUserAndField(user,field);
        if(fieldValue==null) fieldValue = new FieldValue(user,field);
        fieldValueRepo.delete(fieldValue);
        if(fieldValueRepo.countByField(field)==0){
            fieldRepo.delete(fieldId);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isInContacts(User user) {
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
        return userRepo.findUserByUserName(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
