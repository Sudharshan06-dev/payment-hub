package com.notificationservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.notificationservice.event.PaymentEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @KafkaListener(topics = "payment-confirmation")
    public void listen(PaymentEvent paymentEvent) {

        log.info("Received Payment Event: {}", paymentEvent);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo("msudharshan06@gmail.com");
            helper.setSubject(
                    "Payment Successful | Transaction " + paymentEvent.getTransactionReference()
            );

            helper.setText(
                    String.format("""
                            Hi,

                            We’re happy to let you know that your payment was processed successfully.

                            Transaction Number : %s
                            Payment Method     : %s
                            Amount Paid        : $%s
                            Payment Date       : %s

                            Thank you for shopping with Spring Shop!

                            Best Regards,
                            Spring Shop Team
                            """,
                            paymentEvent.getTransactionReference(),
                            paymentEvent.getPaymentMethod(),
                            paymentEvent.getPaymentAmount(),
                            paymentEvent.getPaymentDate()
                    ),
                    false
            );
        };

        try {
            mailSender.send(messagePreparator);
            log.info("Payment confirmation email sent to {}", paymentEvent.getUserEmail());
        } catch (MailException ex) {
            log.error("Failed to send payment email", ex);
            throw new IllegalStateException(
                    "Failed to send payment email to " + paymentEvent.getUserEmail(), ex
            );
        }
    }
}

