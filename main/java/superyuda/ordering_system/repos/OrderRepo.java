package superyuda.ordering_system.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import superyuda.ordering_system.entities.Branch;
import superyuda.ordering_system.entities.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends CrudRepository<Order,Long> {

    boolean existsBySalId(Long salId);

    List<Order> findAllByDispatchAndFinalStatus(Branch dispatch, boolean finalStatus);

    List<Order> findAllByDispatch(Branch dispatch);

    Optional<Order> findBySalId(Long salId);

}
