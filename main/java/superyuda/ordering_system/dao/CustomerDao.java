package superyuda.ordering_system.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import superyuda.ordering_system.entities.Customer;
import superyuda.ordering_system.exceptions.CustomerNotExistException;
import superyuda.ordering_system.services.EmailServiceImpl;
import superyuda.ordering_system.repos.CustomerRepo;

import java.util.List;

@Component
public class CustomerDao {
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    private EmailServiceImpl emailService;

    public Customer addOrGetCustomer(int msgNum) throws Exception {
        String msg = emailService.getOneMail(msgNum);
        List<String> customerDetails = emailService.getCustomerDetailsList(msg);
        Customer toReturn = new Customer(customerDetails.get(0), customerDetails.get(1), customerDetails.get(2));
        if (!customerRepo.existsByPhoneNumber(toReturn.getPhoneNumber())) {
            return customerRepo.save(toReturn);
        }
         return customerRepo.getByPhoneNumber(toReturn.getPhoneNumber()).get();
    }

    public Customer deleteCustomer(Long customerId) throws CustomerNotExistException {
        return customerRepo.findById(customerId).map(customer -> {
            Customer toReturn = customer;
            customerRepo.delete(customer);
            return toReturn;
        }).orElseThrow(() -> new CustomerNotExistException("Customer does not exist by id: " + customerId + "."));
    }

}
