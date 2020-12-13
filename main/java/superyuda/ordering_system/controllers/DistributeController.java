package superyuda.ordering_system.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import superyuda.ordering_system.dao.DispatchDao;
import superyuda.ordering_system.dao.OrderDao;
import superyuda.ordering_system.entities.Branch;
import superyuda.ordering_system.entities.Order;
import superyuda.ordering_system.exceptions.OrderDoesntExistsException;
import superyuda.ordering_system.services.EmailServiceImpl;
import superyuda.ordering_system.services.MailService;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;


import static superyuda.ordering_system.controllers.ControllerUtils.*;


@Controller
@RequestMapping({"mishlochim", " ", "/"})
public class DistributeController {

    @Autowired
    private DispatchDao dispatchDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    EmailServiceImpl emailServiceImp;
    @Autowired
    MailService mailService;

    @GetMapping({"/enter", ""})
    public String enter() {
        return "enter";
    }

    @RequestMapping(value = "welcome", method = {RequestMethod.POST, RequestMethod.GET})
    public String hello(HttpServletRequest request, Model model) {
        String name = request.getParameter(BRANCH);
        String password = request.getParameter(PASSWORD);
        if (!dispatchDao.login(name, password)) {
            model.addAttribute(ERROR, "שם משתמש/סיסמא שגויים. אנא נסה שוב.");
            return "noLogin";
        }
        if (name.equals(ADMIN) && dispatchDao.login(name, password)) {
            request.getSession().setAttribute(BRANCH, dispatchDao.addOrGetBranch(name));
            model.addAttribute(ALL_ORDERS, orderDao.getAdminStats());
            return "admin";
        }
        request.getSession().setAttribute(BRANCH, dispatchDao.addOrGetBranch(name));
        model.addAttribute(
                ORDERS,
                orderDao.getActiveOrdersForToday(dispatchDao.addOrGetBranch(name)));
        return "activeOrders";
    }

    @PostMapping("/seeOrder")
    public String activeOrders(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute(BRANCH) != null) {
            String id = request.getParameter(SINGLE_ORDER);
            Order shown = orderDao.getById(Long.valueOf(id));
            model.addAttribute("header1", String.format("הזמנה מס: %d ", shown.getSalId()));
            model.addAttribute("header2", String.format(" לקוח: %s %s ", shown.getCustomer().getName(), shown.getCustomer().getPhoneNumber()));
            model.addAttribute("header3", String.format("כתובת: %s ", shown.getCustomer().getAddress()));
            model.addAttribute(
                    SINGLE_ORDER,
                    orderDao.getOrder(id));
            model.addAttribute(
                    ORDER_STATUS,
                    orderDao.getOrderStats(id));
            return "seeOrder";
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:noLogin";
    }

    @PostMapping("/adminSeeOrder")
    public String adminActiveOrders(HttpServletRequest request, Model model) {
        Branch branch = (Branch) request.getSession().getAttribute(BRANCH);
        if (request.getSession().getAttribute(BRANCH) != null && branch.getName().equals(ADMIN)) {
            String id = request.getParameter(SINGLE_ORDER);
            Order shown = orderDao.getById(Long.valueOf(id));
            model.addAttribute("header1", String.format("הזמנה מס: %d ", shown.getSalId()));
            model.addAttribute("header2", String.format(" לקוח: %s %s ", shown.getCustomer().getName(), shown.getCustomer().getPhoneNumber()));
            model.addAttribute("header3", String.format("כתובת: %s ", shown.getCustomer().getAddress()));
            model.addAttribute(
                    SINGLE_ORDER,
                    orderDao.getOrder(id));
            model.addAttribute(
                    ORDER_STATUS,
                    orderDao.getOrderStats(id));
            return "adminSeeOrder";
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:error";
    }

    @RequestMapping(value = "/updateOrder", method = {RequestMethod.POST})
    @Transactional
    public String update(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute(BRANCH) != null) {
            boolean pickUp = request.getParameter(PICKUP_STATUS) != null;
            boolean charge = request.getParameter(CHARGE_STATUS) != null;
            boolean sent = request.getParameter(SEND_STATUS) != null;
            Long id = Long.valueOf(request.getParameter(ORDER_ID));
            Order updated = orderDao.updateStats(pickUp, charge, sent, id);
            model.addAttribute(ADMIN, request.getAttribute(ADMIN));
            model.addAttribute(
                    ORDERS,
                    orderDao.getActiveOrdersForToday(updated.getDispatch()));
            return "activeOrders";
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:/error";

    }

    @RequestMapping(value = "adminUpdate", method = {RequestMethod.POST})
    @Transactional
    public String adminUpdate(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute(BRANCH) != null) {
            Long id = Long.valueOf(request.getParameter(ORDER_ID));
            Branch previous = orderDao.getById(id).getDispatch();
            Branch toUpdate = null;
            boolean pickUp = request.getParameter(PICKUP_STATUS) != null;
            boolean charge = request.getParameter(CHARGE_STATUS) != null;
            boolean sent = request.getParameter(SEND_STATUS) != null;
            if (request.getParameter("gab") != null) {
                toUpdate = (dispatchDao.addOrGetBranch("gab73"));
            }
            if (request.getParameter("benY") != null) {
                toUpdate = (dispatchDao.addOrGetBranch("ben32"));
            }
            if (request.getParameter("benZ") != null) {
                toUpdate = (dispatchDao.addOrGetBranch("benZ3"));
            }
            if (request.getParameter("bart") != null) {
                toUpdate = (dispatchDao.addOrGetBranch("bart6"));
            }
            if (request.getParameter("mac") != null) {
                toUpdate = (dispatchDao.addOrGetBranch("mac72"));
            }
            if (request.getParameter("yis") != null) {
                toUpdate = (dispatchDao.addOrGetBranch("yis1"));
            }
            String name = request.getParameter(BRANCH);
            Branch active = dispatchDao.addOrGetBranch(name);
            if (toUpdate != null) {
                orderDao.updateStats(pickUp, charge, sent, toUpdate, id);
            } else {
                orderDao.updateStats(pickUp, charge, sent, id);
            }
            model.addAttribute(ADMIN, request.getAttribute(ADMIN));
            model.addAttribute(
                    ORDERS,
                    orderDao.getActiveOrdersForToday(previous));
            model.addAttribute(
                    "activeBranch", "הזמנות פתוחות לסניף: " + active.getDisplayName());

            return "adminActiveOrders";
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:error";
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST})
    @Transactional
    public String adminDelete(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute(BRANCH) != null) {
            Long id = Long.valueOf(request.getParameter(ORDER_ID));
            Branch previous = orderDao.getById(id).getDispatch();
            String name = request.getParameter(BRANCH);
            Branch active = dispatchDao.addOrGetBranch(name);
            try {
                Order deleted = orderDao.deleteOrder(id);
                model.addAttribute("success", String.format("הזמנה %d נמחקה בהצלחה.", deleted.getSalId()));
            } catch (OrderDoesntExistsException e) {
                model.addAttribute("noSuccess", e.getMessage());
            }
            model.addAttribute(
                    ORDERS,
                    orderDao.getActiveOrders(previous));
            model.addAttribute("activeBranch", "הזמנות פתוחות לסניף: " + active.getDisplayName());
            return "adminActiveOrders";
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:error";
    }

    @RequestMapping(value = "seeActive", method = {RequestMethod.POST, RequestMethod.GET})
    public String getActive(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute(BRANCH) != null) {

            String name = request.getParameter(BRANCH);
            Branch active = dispatchDao.addOrGetBranch(name);
            model.addAttribute(
                    "activeBranch", "הזמנות פתוחות לסניף: " + active.getDisplayName());
            model.addAttribute(
                    ORDERS,
                    orderDao.getActiveOrdersForToday(active));
            return "adminActiveOrders";
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:error";
    }

    @RequestMapping(value = "goback", method = {RequestMethod.POST, RequestMethod.GET})
    public String goBack(HttpServletRequest request, Model model) {
        Branch branch = (Branch) request.getSession().getAttribute(BRANCH);
        if (branch != null && branch.getName().equals(ADMIN)) {
            if (branch.getName().equals("admin")) {
                model.addAttribute(ALL_ORDERS, orderDao.getAdminStats());
                return "admin";
            }

        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:error";
    }

    @RequestMapping(value = "change", method = {RequestMethod.POST, RequestMethod.GET})
    public String changeBranch(HttpServletRequest request, Model model) {
        Branch branch = (Branch) request.getSession().getAttribute(BRANCH);
        Order order = (Order) request.getAttribute("order");
        Branch orderBranch = (Branch) request.getAttribute("dispatch");
        System.out.println(order);
        if (branch != null) {
            if (branch.getName().equals("admin")) {
                request.getSession().setAttribute(BRANCH, branch);
                model.addAttribute("order", order);
                model.addAttribute("dispatch", orderBranch);
                return "changeBranch";
            }
        }
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        model.addAttribute("error", "הודעה: " + throwable.getMessage());
        return "redirect:error";
    }

    @GetMapping("exit")
    public String exit(HttpServletRequest request) {
        request.getSession().removeAttribute(BRANCH);
        return "redirect:enter";
    }

    @PostMapping("errorHandler")
    public String errorHandler(HttpServletRequest request, Model model) {
        String name = request.getParameter(BRANCH);
        String password = request.getParameter(PASSWORD);
        model.addAttribute(BRANCH, name);
        model.addAttribute(PASSWORD, password);
        return "redirect:enter";
    }

}