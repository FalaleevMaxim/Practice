package contactBook.dao;

import contactBook.model.User;

import java.util.Map;
import java.util.Set;

public interface UserDao {
    User getUserById(int id);
    User getUserByName(String userName);
    Set<User> searchByRealName(String realName);
    Set<User> searchByProperties();
    int addUser(User user);
    boolean removeUserById(int id);
    boolean removeUserByName(String name);
}
