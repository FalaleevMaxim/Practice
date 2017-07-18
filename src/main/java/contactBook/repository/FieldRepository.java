package contactBook.repository;

import contactBook.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field,Integer> {

    Field findFieldByName(String name);
}
