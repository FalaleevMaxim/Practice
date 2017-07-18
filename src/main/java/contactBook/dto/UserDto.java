package contactBook.dto;

import contactBook.model.FieldValue;
import contactBook.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDto {
    private int id;
    private String nick;
    private String firstName;
    private String lastName;
    private String email;
    private String number;
    private Set<FieldValueDto> fieldValues = new HashSet<>();

    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.nick = user.getNick();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.number = user.getPhone();
        this.setFieldValues(user.getFields().stream().map(FieldValueDto::new).collect(Collectors.toSet()));
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<FieldValueDto> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(Set<FieldValueDto> fieldValues) {
        this.fieldValues = fieldValues;
    }
}
