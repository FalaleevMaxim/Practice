package contactBook.repository;

import contactBook.model.Picture;
import contactBook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PictureRepository extends JpaRepository<Picture,Integer> {
}
