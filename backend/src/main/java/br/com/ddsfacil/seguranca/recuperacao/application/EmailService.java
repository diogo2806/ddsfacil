package br.com.ddsfacil.seguranca.recuperacao.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final EmailPropriedades propriedades;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailService(EmailPropriedades propriedades, ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.propriedades = propriedades;
        this.mailSenderProvider = mailSenderProvider;
    }

    public void enviarRedefinicaoSenha(String destinatario, String linkRedefinicao) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!propriedades.configurado() || mailSender == null) {
            LOGGER.warn("Envio de e-mail desabilitado/não configurado. Link de redefinição para {}: {}",
                    destinatario, linkRedefinicao);
            return;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(propriedades.getRemetente());
            mensagem.setTo(destinatario);
            mensagem.setSubject("DDS Fácil - Redefinição de senha");
            mensagem.setText(
                    "Recebemos uma solicitação para redefinir sua senha no DDS Fácil.\n\n"
                            + "Acesse o link a seguir para criar uma nova senha (válido por 30 minutos):\n"
                            + linkRedefinicao + "\n\n"
                            + "Se você não fez esta solicitação, ignore este e-mail."
            );
            mailSender.send(mensagem);
            LOGGER.info("E-mail de redefinição de senha enviado para {}.", destinatario);
        } catch (Exception ex) {
            LOGGER.error("Falha ao enviar e-mail de redefinição para {}.", destinatario, ex);
        }
    }
}
