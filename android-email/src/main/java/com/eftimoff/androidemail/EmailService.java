package com.eftimoff.androidemail;

import com.eftimoff.androidemail.annotations.Bcc;
import com.eftimoff.androidemail.annotations.Cc;
import com.eftimoff.androidemail.annotations.Email;
import com.eftimoff.androidemail.annotations.Sender;
import com.eftimoff.androidemail.annotations.Subject;
import com.eftimoff.androidemail.annotations.To;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService extends javax.mail.Authenticator {

    private final String username;
    private final String password;
    private Session session;

    public EmailService(final Properties properties, final String username, final String password) {
        this.session = Session.getInstance(properties, this);
        session.setDebug(true);
        this.username = username;
        this.password = password;
    }

    /**
     * Create the implementation of the interface.
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new EmailHandler());
    }

    private class EmailHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            final MethodInfo methodInfo = new MethodInfo(method);
            final Set<String> pathParameters = methodInfo.getPathParameters();
            final MimeMessage message = createMessage(methodInfo, pathParameters, args);
            final MethodInfo.ExecutionType executionType = methodInfo.getExecutionType();
            switch (executionType) {
                case ASYNC:
                    System.out.println("MESAGE : " + message.getSender());
                    Transport.send(message);

                    break;
                default:
                    throw new

                            IllegalStateException("Unknown response type: " + executionType);
            }
            return null;
        }

        private MimeMessage createMessage(MethodInfo methodInfo, final Set<String> pathParameters, final Object[] args) throws MessagingException {
            final MimeMessage message = new MimeMessage(session);

            final Email emailAnnotation = methodInfo.getEmailAnnotation();
            final String emailValue = changeIfInParameters(emailAnnotation.value(), pathParameters, args);
            final String emailType = emailAnnotation.type();
            message.setContent(emailValue, emailType);

            final Sender senderAnnotation = methodInfo.getSenderAnnotation();
            if (senderAnnotation != null) {
                final String senderValue = changeIfInParameters(senderAnnotation.value(), pathParameters, args);
                final InternetAddress address = new InternetAddress(senderValue);
                message.setSender(address);
            } else {
                final InternetAddress address = new InternetAddress(username);
                message.setSender(address);
            }

            final To toAnnotation = methodInfo.getToAnnotation();
            final String toText = changeIfInParameters(toAnnotation.value(), pathParameters, args);
            message.setRecipients(Message.RecipientType.TO, toText);

            final Cc ccAnnotation = methodInfo.getCcAnnotation();
            if (ccAnnotation != null) {
                final String ccText = changeIfInParameters(ccAnnotation.value(), pathParameters, args);
                message.setRecipients(Message.RecipientType.CC, ccText);
            }

            final Bcc bccAnnotation = methodInfo.getBccAnnotation();
            if (bccAnnotation != null) {
                final String bccText = changeIfInParameters(bccAnnotation.value(), pathParameters, args);
                message.setRecipients(Message.RecipientType.BCC, bccText);
            }

            final Subject subjectAnnotation = methodInfo.getSubjectAnnotation();
            if (subjectAnnotation != null) {
                final String subjectValue = changeIfInParameters(subjectAnnotation.value(), pathParameters, args);
                final String subjectCharset = subjectAnnotation.charset();
                message.setSubject(subjectValue, subjectCharset);
            }

            return message;
        }

        private String changeIfInParameters(String value, final Set<String> pathParameters, final Object[] args) {
            int i = 0;
            for (Iterator<String> iterator = pathParameters.iterator(); iterator.hasNext(); ) {
                final String next = iterator.next();
                final Object arg = args[i++];
                value = value.replace("{" + next + "}", arg.toString());
            }
            return value;
        }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }


}
