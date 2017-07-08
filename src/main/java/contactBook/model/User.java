package contactBook.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class User {
    private int id;
    private String userName;
    private String displayName;
    private String firstName;
    private String lastName;
    private String password;
    private String mail;
    private Map<String,String> otherProperties = new HashMap<>();

    public User(){}

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isReal() {
        return userName!=null;
    }

    public void setNameAndPassword(String userName,String password){
        this.userName = userName;
        this.password = password;
    }

    public String getDisplayName() {
        if(displayName!=null) return displayName;
        return firstName+" "+lastName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getProperty(String propertyName){
        return otherProperties.get(propertyName);
    }

    public Set<String> getPropertyNames(){
        return otherProperties.keySet();
    }

    public Map<String, String> getOtherProperties() {
        return otherProperties;
    }

    public void setProperty(String key, String value){
        otherProperties.put(key,value);
    }
}
