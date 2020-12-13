package superyuda.ordering_system.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import superyuda.ordering_system.entities.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepo extends CrudRepository<Customer,Long> {

    Optional<Customer> getByName(String name);

    Optional<Customer> getByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}
