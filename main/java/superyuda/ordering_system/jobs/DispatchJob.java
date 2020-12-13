package superyuda.ordering_system.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import superyuda.ordering_system.dao.OrderDao;
import superyuda.ordering_system.exceptions.OrderAlreadyExistsException;
import superyuda.ordering_system.services.MailService;

import javax.mail.MessagingException;

@Component
@Scope("singleton")
public class DispatchJob implements Runnable {
    @Autowired
    private MailService mailService;

    @Autowired
    private OrderDao orderDao;

    private final String emailAddress = System.getenv("USER_NAME");
    private final String emailPass = System.getenv("PWD");
    private boolean quit;
    private int msgCount;

    @Override
    public void run() {
        quit = false;
        msgCount = 0;

        orderDao.initiateBranches();
        while (!quit) {
            try {
                mailService.login("imap.gmail.com", emailAddress, emailPass);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (msgCount < mailService.getMessageCount() && !quit) {
                if (msgCount == mailService.getMessageCount()){
                  break;
                }
                try {
                    orderDao.addOrder(msgCount);
                    msgCount++;
                }catch ( StringIndexOutOfBoundsException e1){
                    mailService.sendSimpleMessage("alakapaka@gmail.com", "message num : " + msgCount + " gives out of bounds exc","check it out");
                    e1.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OrderAlreadyExistsException e) {
                    msgCount++;
                    e.printStackTrace();
                }
            }
            try {
                mailService.logout();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}