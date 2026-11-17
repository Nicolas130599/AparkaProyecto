package com.aparka.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    private static final String REMITENTE_GMAIL =
            "aparkasystem@gmail.com";

    public static void enviarCorreo(
            String destinatario,
            String asunto,
            String cuerpoMensaje
    ) throws MessagingException {

        String modo = System.getenv("APARKA_EMAIL_MODE");
        boolean pruebas = "MAILPIT".equalsIgnoreCase(modo);

        Properties props = new Properties();
        String remitente;
        Session session;

        if (pruebas) {

            // SMTP local: no utiliza Gmail ni Internet.
            props.put("mail.smtp.host", "127.0.0.1");
            props.put("mail.smtp.port", "1025");
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.ssl.enable", "false");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");

            remitente = "aparka-pruebas@localhost";
            session = Session.getInstance(props);

        } else if ("GMAIL".equalsIgnoreCase(modo)) {

            // SMTP habitual de APARKA.
            String password =
                    System.getenv("APARKA_EMAIL_PASSWORD");

            if (password == null || password.isBlank()) {
                throw new MessagingException(
                    "No se configuró APARKA_EMAIL_PASSWORD."
                );
            }

            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");

            remitente = REMITENTE_GMAIL;

            session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {

                        return new PasswordAuthentication(
                            REMITENTE_GMAIL,
                            password
                        );
                    }
                }
            );

        } else {
            throw new MessagingException(
                "Configura APARKA_EMAIL_MODE como MAILPIT o GMAIL."
            );
        }

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(remitente));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(destinatario)
        );
        message.setSubject(asunto, "UTF-8");
        message.setText(cuerpoMensaje, "UTF-8");

        Transport.send(message);
    }
}
