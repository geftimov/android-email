package com.eftimoff.androidemail;

import java.util.Properties;

public class TestRunner {

    public static void main(String[] args) {

        final Properties properties = getProperties();
        final EmailService emailService = new EmailService(properties, "jokatavr@gmail.com", "");
        final Test test = emailService.create(Test.class);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                test.firstMethod("ASD", "123", new Callback() {
                    @Override
                    public void success() {
                        System.out.println("success");
                    }

                    @Override
                    public void failure(EmailError emailError) {
                        System.out.println("failure");
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    private static Properties getProperties() {
        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        return props;
    }
}
