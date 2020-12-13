package superyuda.ordering_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EmailServiceImpl {


    @Autowired
    MailService mailService;

    public String getOneMail(int msgNum) throws Exception {

        List<Message> messages = Arrays.asList(mailService.getMessages());
        return getMessage(messages, msgNum);
    }

    public String getOneHeader(int msgNum) throws Exception {

        List<Message> messages = Arrays.asList(mailService.getMessages());
        return getHeader(messages, msgNum);
    }


    public String getCustomerAddress(String customerDetails) {
        String cutFrom = "על ההזמנה להגיע לכתובת";
        int startPoint = customerDetails.indexOf(cutFrom) + cutFrom.length();
        return customerDetails.substring(startPoint, customerDetails.indexOf("ב-<"));
    }


    public List<String> getCustomerDetailsList(String customerDetails) {
        List<String> toReturn = new ArrayList<>();
        toReturn.add(getCustomerName(customerDetails));
        toReturn.add(getCustomerPhone(customerDetails));
        toReturn.add(getCustomerAddress(customerDetails));
        return toReturn;
    }

    public Long getSalId(String theMsg) {
        String starter = "סופר יודה אונליין - הזמנה";
        int startPoint = theMsg.indexOf("סופר יודה אונליין - הזמנה") + starter.length();
        String clean = theMsg.substring(startPoint, theMsg.indexOf("ב\"סל קניות\"")).trim();
        return Long.valueOf(clean);
    }

    private List<String> lineAsListGen(String line, int endPoint, String endString) {
        List<String> lineAsList = new ArrayList<>();
        lineAsList.add(line.substring(0, endPoint));
        lineAsList.add(line.substring(endPoint, endPoint + 3));
        lineAsList.add(line.substring(endPoint + 3, line.indexOf(endString) + endString.length()));
        if (line.endsWith(endString)) {
            return lineAsList;
        }
        String afterCut = line.substring(endString.length() + line.indexOf(endString));
        while (afterCut.startsWith(" ")) {
            afterCut = afterCut.substring(1);
        }
        if (afterCut.contains(" ") && !afterCut.startsWith(" ")) {
            lineAsList.add(afterCut.substring(0, afterCut.indexOf(" ")));
        } else {
            lineAsList.add(afterCut);
        }
        lineAsList.add(getPrice(lineAsList.get(1), lineAsList.get(3)));

        return lineAsList;
    }

    private String getPrice(String quantity, String price) {
        double dPrice = Double.valueOf(price);
        double dQuantity = Double.valueOf(quantity);
        Double result = Math.floor((dPrice * dQuantity) * 100) / 100;
        return String.valueOf(result);
    }

    public String getMessage(List<Message> messages, int msgNum) throws MessagingException, IOException {
        Message message = messages.get(msgNum);
        Object content = message.getContent();
        return contentMaker(content);
    }

    public String getHeader(List<Message> messages, int msgNum) throws MessagingException, IOException {
        Message message = messages.get(msgNum);
        Object content = message.getSubject();
        return contentMaker(content);
    }

    private String contentMaker(Object content) throws MessagingException, IOException {
        if (content instanceof String) {
            return content.toString();
        } else if (content instanceof Multipart) {
            Multipart multiPart = (Multipart) content;
            int multiPartCount = multiPart.getCount();
            for (int i = 0; i < multiPartCount; i++) {
                BodyPart bodyPart = multiPart.getBodyPart(i);
                Object o;
                o = bodyPart.getContent();
                if (o instanceof String) {
                    return o.toString();
                }
            }
        }
        return "error";
    }

    public String getCustomerName(String message) {
        String cropped = message.substring(346);
        return cropped.substring(0, cropped.indexOf("&"));

    }

    public String getCustomerPhone(String message) {
        message = message.substring(message.indexOf(getCustomerName(message)) + getCustomerName(message).length());
        String cropped = message.substring(94);
        return cropped.substring(0, cropped.indexOf("</span>"));
    }

    public String getOrderTime(String message) {
        message = message.substring(message.indexOf(getCustomerAddress(message)) + getCustomerAddress(message).length());
        String cropped = message.substring(89);
        return cropped.substring(0, cropped.indexOf("</span>"));
    }


    public String getCleanOrder(String message) {
        String begin = "<tbody>";
        message = message.substring(message.indexOf(begin) + begin.length());
        return message.substring(0, message.indexOf("</strong>"));

    }

    public  String getCustomerNotes(String message) {
        String begin = "<strong>";
        message = message.substring(message.indexOf(begin) + begin.length());
        return message.substring(0, message.indexOf("</strong>"));
}

    public List<String> getLines(String message) {
        String tr = "<tr>";
        String etr = "</tr>";
        List<String> lines = new ArrayList<>();
        while (message.contains(tr)) {
            int start = (0 + tr.length());
            int end = message.indexOf(etr);
            lines.add(message.substring(start, end));
            message = message.substring(message.indexOf(etr) + etr.length());
        }
        return lines;
    }

    public List<List<String>> lineBreaker(List<String> lines) {
        String td = "<td>";
        String etd = "</td>";
        List<List<String>> toReturn = new ArrayList<>();
        lines.forEach(line -> {
            List<String> breaked = new ArrayList<>();
            while (line.contains(td)) {
                int start = (0 + td.length());
                int end = line.indexOf(etd);
                if (line.contains("img src=")) {
                    breaked.add(line.substring(line.indexOf("src=") + 5, line.indexOf("/>") - 1));
                    line = line.substring(line.indexOf(etd) + etd.length());
                } else {
                    breaked.add(line.substring(start, end));
                    line = line.substring(line.indexOf(etd) + etd.length());
                }
            }
            toReturn.add(breaked);
        });
        return toReturn;
    }

    public void logout() {
        try {
            mailService.logout();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendAlert( String to, String subject, String text) {
        mailService.sendSimpleMessage(to,  subject,text);
    }
}