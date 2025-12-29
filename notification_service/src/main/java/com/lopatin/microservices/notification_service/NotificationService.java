package com.lopatin.microservices.notification_service;

import com.lopatin.order_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.util.Utf8;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent){

        log.info("Got message {} from order-placed topic", orderPlacedEvent);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom("microservices@gmail.com");
            helper.setTo(orderPlacedEvent.getEmail().toString());
            helper.setSubject(String.format("Your order with OrderNumber %s is successfully placed!", orderPlacedEvent.getOrderNumber()));
            helper.setText(String.format(
                    """
                    Hi %s, %s
                    
                    Your order with order number %s is now placed successfully.
                    
                    Best Regards
                    Your Service!
                    
                    """,
                    orderPlacedEvent.getFirstName(),
                    orderPlacedEvent.getLastName(),
                    orderPlacedEvent.getOrderNumber()
            ));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Order Notification email sent!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail", e);
        }
    }


}
