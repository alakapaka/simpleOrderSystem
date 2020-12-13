package superyuda.ordering_system.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import superyuda.ordering_system.entities.Branch;
import superyuda.ordering_system.entities.Customer;
import superyuda.ordering_system.entities.Order;
import superyuda.ordering_system.exceptions.OrderAlreadyExistsException;
import superyuda.ordering_system.exceptions.OrderDoesntExistsException;
import superyuda.ordering_system.services.DistanceTime;
import superyuda.ordering_system.services.EmailServiceImpl;
import superyuda.ordering_system.repos.OrderRepo;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@Component
public class OrderDao {
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private DistanceTime distanceTime;
    @Autowired
    private DispatchDao dispatchDao;

    @Transactional
    public Order addOrder(int msgNum) throws Exception, OrderAlreadyExistsException {
        String theMsg = emailService.getOneMail(msgNum);
        String theHeader = emailService.getOneHeader(msgNum);
        Long salId = emailService.getSalId(theHeader);
        if (orderRepo.existsBySalId(salId)) {
            throw new OrderAlreadyExistsException("Order " + salId + " already exists in the system.");
        }
        if (!orderRepo.existsBySalId(salId - 1)) {
            emailService.sendAlert("kc.yuda1@gmail.com", "הזמנה " + salId, "מספר ההזמנה אינו עוקב. ייתכן וההזמנה לפני הזמנה זאת אינה התקבלה מסל קניות, בדיקתכם.");
            emailService.sendAlert("barsuper2020@gmail.com", "הזמנה " + salId, "מספר ההזמנה אינו עוקב. ייתכן וההזמנה לפני הזמנה זאת אינה התקבלה מסל קניות, בדיקתכם.");
        }
        String order = emailService.getCleanOrder(theMsg);
        String orderTime = emailService.getOrderTime(theMsg);
        Customer customer = customerDao.addOrGetCustomer(msgNum);
        String notes = emailService.getCustomerNotes(theMsg);
        String dispatchName = distanceTime.getClosestBranch(customer.getAddress());
        Branch dispatch = dispatchDao.addOrGetBranch(dispatchName);
        Order newOrder = new Order(salId, customer, order, notes, orderTime, dispatch);
        dispatchDao.addOrder(dispatch.getId(), newOrder);
        orderRepo.save(newOrder);
        return newOrder;
    }

    public List<Order> getActiveOrders(Branch dispatch) {
        return orderRepo.findAllByDispatchAndFinalStatus(dispatch, false);
    }

    public List<Order> getActiveOrdersForToday(Branch dispatch) {
        return orderRepo.findAllByDispatchAndFinalStatus(dispatch, false)
                .stream()
                .filter(
                        order -> cleanDate(order).equals(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public List<Order> getFinishedOrders(Branch dispatch) {
        return orderRepo.findAllByDispatchAndFinalStatus(dispatch, true);
    }

    public List<Order> getFinishedOrdersForToday(Branch dispatch) {
        return orderRepo.findAllByDispatchAndFinalStatus(dispatch, true)
                .stream()
                .filter(
                        order -> cleanDate(order).equals(LocalDate.now()))
                .collect(Collectors.toList());
    }


    public List<List<String>> getOrder(String id) {
        Order order = orderRepo.findById(Long.valueOf(id)).get();
        String content = order.getOrderBody();
        List<String> lines = emailService.getLines(content);
        List<List<String>> broken = emailService.lineBreaker(lines);

        return broken;
    }

    public Order getOrderStats(String id) {
        Order order = orderRepo.findById(Long.valueOf(id)).get();

        return order;
    }

    @Transactional
    public Order updateStats(boolean pickUp, boolean charge, boolean sent, Long id) {
        Order order = orderRepo.findById(id).get();
        order.setPickUpStatus(pickUp);
        order.setChargeStatus(charge);
        order.setSentStatus(sent);
        if (pickUp && charge && sent) {
            order.setFinalStatus(true);
        } else {
            order.setFinalStatus(false);
        }
        return order;
    }

    @Transactional
    public Order updateStats(boolean pickUp, boolean charge, boolean sent, Branch dispatch, Long id) {
        Order order = orderRepo.findById(id).get();
        order.setDispatch(dispatch);
        order.setPickUpStatus(pickUp);
        order.setChargeStatus(charge);
        order.setSentStatus(sent);
        if (pickUp && charge && sent) {
            order.setFinalStatus(true);
        } else {
            order.setFinalStatus(false);
        }
        return order;
    }

    public Order getById(Long id) {
        return orderRepo.findById(id).get();
    }

    public void initiateBranches() {
        dispatchDao.initiateBranches();
    }

    public List<Order> getAllForAdmin(Branch dispatch) {
        return orderRepo.findAllByDispatch(dispatch);
    }
    public List<Order> getAllForAdminForToday(Branch dispatch) {
        return orderRepo.findAllByDispatch(dispatch)
                .stream()
                .filter(
                        order -> cleanDate(order).equals(LocalDate.now()))
                .collect(Collectors.toList());
    }

    public List<Order> getAll() {
        return (List<Order>) orderRepo.findAll();
    }

    public Map<Branch, List<Integer>> getPreformance(Branch dispatch) {
        List<Integer> branchStats = new ArrayList<>();
        Integer active = getActiveOrdersForToday(dispatch).size();
        Integer finished = getFinishedOrdersForToday(dispatch).size();
        Integer total = getAllForAdminForToday(dispatch).size();
        branchStats.add(active);
        branchStats.add(finished);
        branchStats.add(total);
        Map<Branch, List<Integer>> toReturn = new HashMap<>();
        toReturn.put(dispatch, branchStats);
        return toReturn;
    }

    public Map<Branch, List<Integer>> getAdminStats() {
        Map<Branch, List<Integer>> stats = new HashMap<>();
        List<Branch> branches = dispatchDao.getAll();
        Collections.sort(branches);
        branches.forEach(branch -> stats.put(branch, getPreformance(branch).get(branch)));
        return stats;
    }

    @Transactional
    public Order deleteOrder(Long id) throws OrderDoesntExistsException {
        return orderRepo.findById(id).map(order -> {
            List<Order> customerNewOrders = order.getCustomer().getOrders();
            List<Order> branchNewOrders = order.getDispatch().getOrders();
            customerNewOrders.remove(order);
            branchNewOrders.remove(order);
            order.getCustomer().setOrders(customerNewOrders);
            order.getDispatch().setOrders(branchNewOrders);
            orderRepo.deleteById(id);
            return order;
        }).orElseThrow(() -> new OrderDoesntExistsException("Order by id: " + id + "doesnt exists. Cannot complete delete process."));
    }

    private LocalDate cleanDate(Order order) {

        String orderTime = order.getOrderTime();
        if (orderTime.contains("2020")){
        int day = Integer.valueOf(orderTime.substring(0, orderTime.indexOf("/")));
        String orderTime2 = orderTime.substring(orderTime.indexOf("/") + 1);
        int month = Integer.valueOf(orderTime2.substring(0, orderTime2.indexOf("/")));
        return LocalDate.of(2020, month, day);
        }
        if(order.isFinalStatus()){
            return LocalDate.now().minusDays(1);
        }
        return LocalDate.now();
    }
}
