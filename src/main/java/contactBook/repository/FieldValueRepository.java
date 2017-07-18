package contactBook.repository;

import contactBook.model.Field;
import contactBook.model.FieldValue;
import contactBook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldValueRepository extends JpaRepository<FieldValue,Integer> {
    int countByField(Field field);

    FieldValue findByUserAndField(User user, Field field);
}
