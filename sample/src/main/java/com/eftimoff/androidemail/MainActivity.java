package com.eftimoff.androidemail;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import java.util.Properties;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Properties properties = getProperties();
        final EmailService emailService = new EmailService(properties, "jokatavr@gmail.com", "");
        final EmailServiceTest test = emailService.create(EmailServiceTest.class);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                test.spartaEmail("Georgi", "opelastra100@gmail.com", new Callback() {
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
