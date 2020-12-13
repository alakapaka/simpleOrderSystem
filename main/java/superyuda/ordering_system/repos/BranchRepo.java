package superyuda.ordering_system.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import superyuda.ordering_system.entities.Branch;

import java.util.Optional;

@Repository
public interface BranchRepo extends CrudRepository<Branch,Long> {

    Optional<Branch> getByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndPassword(String name, String password);

}
