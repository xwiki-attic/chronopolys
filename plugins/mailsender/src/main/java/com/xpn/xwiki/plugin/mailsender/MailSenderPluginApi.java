package com.xpn.xwiki.plugin.mailsender;

import java.util.List;

import org.apache.velocity.VelocityContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.plugin.PluginApi;

public class MailSenderPluginApi extends PluginApi
{

    public MailSenderPluginApi(MailSenderPlugin plugin, XWikiContext context)
    {
        super(plugin, context);

    }
    public MailSenderPlugin getMailSenderPlugin()
    {
        return (MailSenderPlugin)getPlugin();
    }

    /**
     * Sends an HTML mail, with a list of attachments
     * 
     * @see #sendHTMLMessage(java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.util.List)
     * @param to the recipient of the message
     * @param from the sender
     * @param subject the subject of the message
     * @param body the body content of the mail
     * @param alternative the alternative text offered to the mail client
     * @param attachments List of com.xpn.xwiki.api.Attachment that will be attached to the mail.
     * @return 0 on success, -1 on failure. on failure the error message is stored in XWiki context
     */
    public int sendHtmlMessage(String from, String to, String cc, String bcc, String subject,
        String body, String alternative, List attachments)
    {
        try {
            Mail email = new Mail();
            email.setSubject(subject);
            email.setFrom(from);
            email.setTo(to);
            email.setCc(cc);
            email.setBcc(bcc);
            email.setTextPart(alternative);
            email.setHtmlPart(body);
            email.setAttachments(attachments);
            getMailSenderPlugin().sendMail(email);
            return 0;
        } catch (Exception e) {
            context.put("error", e.getMessage());
            getMailSenderPlugin().getLogger().error("sendHtmlMessage", e);
            return -1;
        }
    }



    /**
     * Sends a simple text plain mail
     * 
     * @param to the recipient of the message
     * @param from the sender
     * @param subject the subject of the message
     * @param message the body of the message
     * @return 0 on success, -1 on failure. on failure the error message is stored in XWiki context
     */
    public int sendTextMessage(String from, String to, String subject, String message)
    {
        try {
             Mail email = new Mail();
             email.setSubject(subject);
             email.setTextPart(message);
             email.setFrom(from);
             email.setTo(to);
             
             getMailSenderPlugin().sendMail(email);
            return 0;
        } catch (Exception e) {
            context.put("error", e.getMessage());
            getMailSenderPlugin().getLogger().error("sendTextMessage", e);
            return -1;
        }
    }

    /**
     * Sends a simple text plain mail with a list of files attachments
     * 
     * @param to the recipient of the message
     * @param from the sender
     * @param subject the subject of the message
     * @param message the body of the message
     * @param attachments List of com.xpn.xwiki.api.Attachment that will be attached to the mail.
     * @return 0 on success, -1 on failure. on failure the error message is stored in XWiki context
     */
    public int sendTextMessage(String from, String to, String cc, String bcc, String subject,
        String message, List attachments)
    {
        try {
            Mail email = new Mail();
            email.setSubject(subject);
            email.setTextPart(message);
            email.setFrom(from);
            email.setTo(to);
            email.setCc(cc);
            email.setBcc(bcc);
            email.setAttachments(attachments);
            getMailSenderPlugin().sendMail(email);
            return 0;
        } catch (Exception e) {
            context.put("error", e.getMessage());
            getMailSenderPlugin().getLogger().error("sendTextMessage", e);
            return -1;
        }
    }
    /**
     * Uses an XWiki document to build the message subject and context, based on variables stored in
     * the VelocityContext.
     * 
     * @param templateDocFullName Full name of the template to be used (example:
     *            XWiki.MyEmailTemplate). The template needs to have an XWiki.Email object attached
     * @param fromAddr
     * @param toAddr
     * @param bccAddr
     * @param language
     * @param vcontext
     * @param context
     * @return true if email sent, false otherwise
     * @throws  0 on success, -1 on failure. on failure the error message is stored in XWiki context
     */
    public int sendMessageFromTemplate(String from, String to, String cc, String bcc,
        String language, String documentFullName, VelocityContext vcontext)
    {
        try {
            int result = getMailSenderPlugin().sendMailFromTemplate(documentFullName, from, to, cc, bcc, language, vcontext);
            return result;
        } catch (Exception e) {
            context.put("error", e.getMessage());
            getMailSenderPlugin().getLogger().error("sendMessageFromTemplate", e);
            return -1;
        }
    }

}
