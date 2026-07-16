package com.Transami.Transami.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${application.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${application.mail.from-address:no-reply@FleetVision.com}")
    private String fromAddress;

    @Value("${application.mail.from-name:FleetVision - Ne pas repondre}")
    private String fromName;

    public void sendOtpEmail(String toEmail, String otpCode) {
        if (!mailEnabled) {
            log.warn("[MODE DEV] Envoi désactivé. OTP pour {} : {}", toEmail, otpCode);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Nom d'affichage "no-reply" — l'adresse technique réelle
            // dépend de la config SMTP (voir remarque sur les alias Gmail)
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("FleetVision - Code de réinitialisation de mot de passe");
            helper.setText(buildHtmlContent(otpCode), true);
            // Empêche les réponses accidentelles à cette adresse
            helper.setReplyTo(fromAddress);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'e-mail OTP à {} : {}", toEmail, e.getMessage());
        }
    }

    private String buildHtmlContent(String otpCode) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Réinitialisation du mot de passe</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f6f8; font-family:'Segoe UI', Arial, sans-serif;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="padding:30px 0;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" style="max-width:480px; background:#ffffff; border-radius:10px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.06);">
                                    <tr>
                                        <td style="background:#1a73e8; padding:24px; text-align:center;">
                                            <h1 style="color:#ffffff; margin:0; font-size:22px;">FleetVision</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px 28px;">
                                            <h2 style="color:#1f2937; font-size:18px; margin-top:0;">Réinitialisation de votre mot de passe</h2>
                                            <p style="color:#4b5563; font-size:14px; line-height:1.6;">
                                                Vous avez demandé la réinitialisation de votre mot de passe. Utilisez le code ci-dessous pour continuer :
                                            </p>
                                            <div style="text-align:center; margin:28px 0;">
                                                <span style="display:inline-block; background:#f0f4ff; color:#1a73e8; font-size:32px; font-weight:bold; letter-spacing:8px; padding:16px 24px; border-radius:8px;">
                                                    %s
                                                </span>
                                            </div>
                                            <p style="color:#6b7280; font-size:13px; line-height:1.6;">
                                                Ce code est valable pendant <strong>10 minutes</strong>. Si vous n'êtes pas à l'origine de cette demande, ignorez simplement cet e-mail.
                                            </p>
                                            <p style="color:#9ca3af; font-size:11px; margin-top:20px; padding-top:16px; border-top:1px solid #f0f0f0;">
                                                Cet e-mail a été envoyé automatiquement, merci de ne pas y répondre.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background:#f9fafb; padding:16px 28px; text-align:center;">
                                            <p style="color:#9ca3af; font-size:12px; margin:0;">&copy; FleetVision. Tous droits réservés.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(otpCode);
    }
}