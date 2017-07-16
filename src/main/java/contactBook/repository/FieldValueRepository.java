package contactBook.repository;

import contactBook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldValueRepository extends JpaRepository<User,Integer> {

}
