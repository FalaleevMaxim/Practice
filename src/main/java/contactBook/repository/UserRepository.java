package contactBook.repository;

import contactBook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserRepository extends JpaRepository<User,Integer> {
    @Query("select u from User u where u.userName is not null")
    Set<User> getRealUsers();

    User findUserByUserName(@Param("name") String userName);
}
