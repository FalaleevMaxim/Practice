package contactBook.repository;

import contactBook.model.Group;
import contactBook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface GroupRepository extends JpaRepository<Group,Integer>{
    @Query("select g from Group g where g.owner = :owner")
    Set<Group> getGroupsByOwner(@Param("owner") User owner);
}