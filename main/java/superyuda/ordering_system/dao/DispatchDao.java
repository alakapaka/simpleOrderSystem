package superyuda.ordering_system.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import superyuda.ordering_system.entities.Branch;
import superyuda.ordering_system.entities.Order;
import superyuda.ordering_system.repos.BranchRepo;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Component
public class DispatchDao {
    @Autowired
    BranchRepo branchRepo;

    public Branch addOrGetBranch(String branchName, String  displaName) {
        if (branchRepo.existsByName(branchName)) {
            return branchRepo.getByName(branchName).get();
        }
        if (branchName.equals("admin")) {
            return branchRepo.save(new Branch("admin", "admin639",displaName));
        }
        Branch branch = new Branch(branchName, branchName + "555",displaName);
        return branchRepo.save(branch);
    }
    public Branch addOrGetBranch(String branchName) {
        if (branchRepo.existsByName(branchName)) {
            return branchRepo.getByName(branchName).get();
        }
        if (branchName.equals("admin")) {
            return branchRepo.save(new Branch("admin", "admin639"));
        }
        Branch branch = new Branch(branchName, branchName + "555");
        return branchRepo.save(branch);
    }

    public Branch getById(Long id) {
        return branchRepo.findById(id).get();
    }

    public boolean login(String name, String password) {
        return branchRepo.existsByNameAndPassword(name, password);
    }

    @Transactional
    public Branch addOrder(Long id, Order newOrder) {
        Branch branch = branchRepo.findById(id).get();
        List<Order> orders = new ArrayList<>();
        if (branch.getOrders() != null) {
            orders = branch.getOrders();
        }
        orders.add(newOrder);
        branch.setOrders(orders);
        return branchRepo.save(branch);
    }

    public void initiateBranches() {
        addOrGetBranch("ben32","בן יהודה 32");
        addOrGetBranch("gab73","אבן גבירול 73");
        addOrGetBranch("yis1","ישעיהו 1");
        addOrGetBranch("mac72","מכבי 72");
        addOrGetBranch("benZ3","בן ציון 3");
        addOrGetBranch("bart6","בארט 6");
        addOrGetBranch("admin","אדמין");
    }

    public List<Branch> getAll() {
        List<Branch> toReturn = new ArrayList<>();
        branchRepo.findAll().forEach(branch -> {
            if (!branch.getName().equals("admin")) {
            toReturn.add(branch);}
        });
        return toReturn;
    }
}
