package com.nova.service;
import com.nova.jobs.BackgroundJob;
import com.nova.jobs.BackgroundJobRepository;


import com.nova.orders.OrderRecord;
import com.nova.orders.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class BackgroundJobProcessor {
    private static final Logger log = LoggerFactory.getLogger(BackgroundJobProcessor.class);
    private final BackgroundJobRepository jobs;
    private final OrderRepository orders;
    private final JavaMailSender mail;
    private final JobNotificationService publisher;
    private final String mailUser;
    private final String mailFrom;

    public BackgroundJobProcessor(
            BackgroundJobRepository jobs,
            OrderRepository orders,
            JavaMailSender mail,
            JobNotificationService publisher,
            @Value("${spring.mail.username:}") String mailUser,
            @Value("${app.mail.from:}") String mailFrom) {
        this.jobs = jobs;
        this.orders = orders;
        this.mail = mail;
        this.publisher = publisher;
        this.mailUser = mailUser;
        this.mailFrom = mailFrom;
    }

    @Async
    @Transactional
    public void process(Long id) {
        BackgroundJob job = jobs.findById(id).orElse(null);
        if (job == null) return;
        try {
            job.processing();
            jobs.save(job);
            publisher.publish(job);
            OrderRecord order = orders.findById(job.getOrderId())
                    .orElseThrow(() -> new IllegalStateException("Order not found"));
            switch (job.getJobType()) {
                case "EMAIL" -> sendOrderEmail(order);
                case "INVOICE" -> generateInvoice(order);
                case "NOTIFICATION" -> log.info("NOVA notification: Order {} payment confirmed", order.getId());
                default -> throw new IllegalArgumentException("Unsupported job type: " + job.getJobType());
            }
            job.complete(job.getJobType() + " task completed");
            jobs.save(job);
            publisher.publish(job);
        } catch (Exception e) {
            job.fail(e.getMessage() == null ? "Task failed" : e.getMessage());
            jobs.save(job);
            publisher.publish(job);
            log.error("Background job {} failed", id, e);
        }
    }

    private void sendOrderEmail(OrderRecord order) {
        String to = order.getContactEmail() != null && !order.getContactEmail().isBlank()
                ? order.getContactEmail() : order.getUser().getEmail();
        if (mailUser.isBlank()) {
            throw new IllegalStateException("Mail credentials are not configured; notification was not sent");
        }
        String sender = mailFrom.isBlank() ? mailUser : mailFrom;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(to);
        message.setSubject("NOVA order confirmation #" + order.getId());
        message.setText("Hello " + order.getUser().getName() + ",\n\n"
                + "Your order #" + order.getId() + " has been paid successfully.\n"
                + "Total: INR " + order.getTotalAmount() + "\n"
                + "Delivery Address: " + order.getDeliveryAddress() + "\n"
                + "Phone: " + order.getRecipientPhone() + "\n\n"
                + "Thank you for shopping with NOVA.");
        try {
            mail.send(message);
        } catch (Exception e) {
            log.error("Failed to send customer order confirmation email to {}", to, e);
        }

        // Also notify store admin if email is different
        if (!mailUser.equalsIgnoreCase(to)) {
            try {
                SimpleMailMessage adminMsg = new SimpleMailMessage();
                adminMsg.setFrom(sender);
                adminMsg.setTo(mailUser);
                adminMsg.setSubject("Store Alert: New Order #" + order.getId() + " - INR " + order.getTotalAmount());
                adminMsg.setText("Hello Store Owner,\n\n"
                        + "A new order has been paid on your website!\n\n"
                        + "Order ID: #" + order.getId() + "\n"
                        + "Customer: " + order.getRecipientName() + " (" + to + ")\n"
                        + "Amount: INR " + order.getTotalAmount() + "\n"
                        + "Delivery Address: " + order.getDeliveryAddress() + "\n"
                        + "Phone: " + order.getRecipientPhone() + "\n\n"
                        + "View order details in the NOVA Admin Dashboard.");
                mail.send(adminMsg);
            } catch (Exception e) {
                log.warn("Failed to send admin notification for order {}: {}", order.getId(), e.getMessage());
            }
        }
    }

    private void generateInvoice(OrderRecord order) throws Exception {
        Path folder = Path.of("invoices");
        Files.createDirectories(folder);
        String html = """
                <html>
                <head><meta charset="UTF-8"><title>NOVA Invoice</title></head>
                <body>
                  <h1>NOVA Studio Invoice</h1>
                  <p>Order: %d</p>
                  <p>Customer: %s</p>
                  <p>Payment status: PAID</p>
                  <p>Total amount: INR %s</p>
                  <h2>Items</h2><pre>%s</pre>
                  <p>Thank you for shopping with NOVA.</p>
                </body>
                </html>
                """.formatted(order.getId(), escape(order.getUser().getName()),
                order.getTotalAmount(), escape(order.getItemsJson()));
        Files.writeString(folder.resolve("invoice-" + order.getId() + ".html"), html, StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
