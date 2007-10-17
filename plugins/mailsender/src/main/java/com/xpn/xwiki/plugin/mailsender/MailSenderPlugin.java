package com.xpn.xwiki.plugin.mailsender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.render.XWikiVelocityRenderer;

public final class MailSenderPlugin extends XWikiDefaultPlugin implements XWikiPluginInterface
{

    public static final String EMAIL_XWIKI_CLASS_NAME = "XWiki.Mail";
    
    public static int ERROR_TEMPLATE_EMAIL_OBJECT_NOT_FOUND = -2;
    public static int ERROR = -1;
    

    public static final String ID = "mailsender";

    private static Log log = LogFactory.getLog(MailSenderPlugin.class);

    protected static final String URL_SEPARATOR = "/";

    public static String[] parseAddresses(String email)
    {
        if (email == null)
            return null;
        email = email.trim();
        String[] emails = email.split(",");
        for (int i = 0; i < emails.length; i++)
            emails[i] = emails[i].trim();
        return emails;

    }

    private static InternetAddress[] toInternetAddresses(String email) throws AddressException
    {
        String[] mails = parseAddresses(email);
        if (mails == null)
            return null;

        InternetAddress[] address = new InternetAddress[mails.length];
        for (int i = 0; i < mails.length; i++) {
            address[i] = new InternetAddress(mails[i]);
        }
        return address;
    }

    protected XWikiContext context;

    public MailSenderPlugin(String name, String className, XWikiContext context)
    {
        super(name, className, context);
        init(context);
        this.context = context;
    }

    public void init(XWikiContext context)
    {
        try {
            initMailClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
            initMailClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected BaseClass initMailClass(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;

        try {
            doc = xwiki.getDocument(EMAIL_XWIKI_CLASS_NAME, context);
        } catch (Exception e) {
            doc = new XWikiDocument();
            String[] spaceAndName = EMAIL_XWIKI_CLASS_NAME.split(".");
            doc.setSpace(spaceAndName[0]);
            doc.setName(spaceAndName[1]);
            needsUpdate = true;
        }

        BaseClass bclass = doc.getxWikiClass();
        bclass.setName(EMAIL_XWIKI_CLASS_NAME);
        needsUpdate |= bclass.addTextField("subject", "Subject", 40);
        needsUpdate |= bclass.addTextField("language", "Language", 5);
        needsUpdate |= bclass.addTextAreaField("text", "Text", 40, 5);
        needsUpdate |= bclass.addTextAreaField("html", "HTML", 40, 5);

        String content = doc.getContent();
        if ((content == null) || (content.equals(""))) {
            needsUpdate = true;
            doc.setContent("#includeForm(\"XWiki.XWikiEmailSheet\"");
        }

        if (needsUpdate)
            xwiki.saveDocument(doc, context);
        return bclass;
    }

    public void addAttachments(Multipart mpart, List attachments)
        throws XWikiException, IOException, MessagingException
    {
        if (attachments != null) {
            Iterator attachmentIt = attachments.iterator();
            while (attachmentIt.hasNext()) {
                Attachment at = (Attachment)attachmentIt.next();
                XWikiAttachment att = at.getAttachment();
                String name = att.getFilename();
                byte[] stream = att.getContent(context);
                File temp = File.createTempFile("tmpfile", ".tmp");
                FileOutputStream fos = new FileOutputStream(temp);
                fos.write(stream);
                fos.close();
                MimeBodyPart part = new MimeBodyPart();
                DataSource source = new FileDataSource(temp);
                part.setDataHandler(new DataHandler(source));
                part.setHeader("Content-ID", "<" + name + ">");
                part.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");
                String mimeType = MimeTypesUtil.getMimeTypeFromFilename(name);
                part.setHeader("Content-Type", " " + mimeType + "; name=\"" + name + "\"");

                part.setFileName(name);
                mpart.addBodyPart(part);
                temp.deleteOnExit();
            }
        }

    }

    private MimeMessage createMimeMessage(Mail mail, Session session) throws MessagingException,
        XWikiException, IOException
    {
        // this will also check for email error
        InternetAddress from = new InternetAddress(mail.getFrom());
        InternetAddress[] to = toInternetAddresses(mail.getTo());
        InternetAddress[] cc = toInternetAddresses(mail.getCc());
        InternetAddress[] bcc = toInternetAddresses(mail.getBcc());

        if ((to == null) && (cc == null) && (bcc == null)) {
            log.info("No recipient -> skipping this email");
            return null;
        }

        MimeMessage message = new MimeMessage(session);
        message.setSentDate(new Date());
        message.setFrom(from);

        if (to != null)
            message.setRecipients(javax.mail.Message.RecipientType.TO, to);

        if (cc != null)
            message.setRecipients(javax.mail.Message.RecipientType.CC, cc);

        if (bcc != null)
            message.setRecipients(javax.mail.Message.RecipientType.BCC, bcc);

        message.setSubject(mail.getSubject(), "UTF-8");

        if (mail.getHtmlPart() != null || mail.getAttachments() != null) {
            Multipart multipart = createMimeMultipart(mail);
            message.setContent(multipart);
        } else {
            message.setText(mail.getTextPart());
        }

        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    public Multipart createMimeMultipart(Mail mail) throws MessagingException, XWikiException,
        IOException
    {

        if (mail.getHtmlPart() == null && mail.getAttachments() != null) {

            Multipart multipart = new MimeMultipart("mixed");
            BodyPart part = new MimeBodyPart();
            part.setContent(mail.getTextPart(), "text/plain");
            multipart.addBodyPart(part);
            addAttachments(multipart, mail.getAttachments());
            return multipart;

        } else {

            Multipart alternativeMultipart = new MimeMultipart("alternative");

            BodyPart part;

            part = new MimeBodyPart();
            part.setText(mail.getTextPart());
            alternativeMultipart.addBodyPart(part);

            // Multipart html_mp = new MimeMultipart("related");

            part = new MimeBodyPart();

            part.setContent(processImageUrls(mail.getHtmlPart()), "text/html");
            part.setHeader("Content-Disposition", "inline");
            part.setHeader("Content-Transfer-Encoding", "quoted-printable");

            alternativeMultipart.addBodyPart(part);

            if (mail.getAttachments() != null && mail.getAttachments().size() > 0) {

                Multipart mixedMultipart = new MimeMultipart("mixed");
                part = new MimeBodyPart();
                part.setContent(alternativeMultipart);
                mixedMultipart.addBodyPart(part);

                addAttachments(mixedMultipart, mail.getAttachments());
                return mixedMultipart;
            }
            return alternativeMultipart;

        }
    }

    protected String evaluate(String property, Context context) throws Exception
    {
        String value = (String) context.get(property);
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, property, value);
        stringWriter.close();
        return stringWriter.toString();
    }

    protected String getFileName(String path)
    {
        return path.substring(path.lastIndexOf(URL_SEPARATOR) + 1);
    }

    public String getName()
    {
        return ID;
    }

    private Properties initProperties()
    {
        Properties properties = new Properties();
        
        
        /*if ( (mailOption.username != null) && (mailOption.username.length() > 0) ) {
            props.put("mail.smtp.auth", "true");
        }*/
        
        properties.put("mail.smtp.port", "25");
        String smtp =
            context != null ? context.getWiki().Param("xwiki.smtp.host", "localhost")
                : "localhost";
        properties.put("mail.smtp.localhost", "localhost");
        properties.put("mail.host","localhost");
        properties.put("mail.smtp.host", smtp);
        properties.put("mail.debug", "false");
        log.debug("smtp: "+smtp);
        return properties;
    }

    public VelocityContext prepareVelocityContext(String fromAddr, String toAddr, String bccAddr,
        VelocityContext vcontext)
    {
        if (vcontext == null)
            vcontext = new VelocityContext();

        vcontext.put("from.name", fromAddr);
        vcontext.put("from.address", fromAddr);
        vcontext.put("to.name", toAddr);
        vcontext.put("to.address", toAddr);
        vcontext.put("to.bcc", bccAddr);
        vcontext.put("bounce", fromAddr);
        return vcontext;

    }

    private String processImageUrls(String html)
    {
        // this method/design has to be improved

        Pattern img =
            Pattern.compile("src=[a-zA-Z0-9/_\"]*.(png|jpg|gif|jpeg){1}",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = img.matcher(html);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String found = matcher.group(0);
            String replace = found.substring(found.lastIndexOf("/") + 1);
            matcher.appendReplacement(sb, "src=\"cid:" + replace);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public boolean sendMail(Mail mailItem) throws MessagingException,
        UnsupportedEncodingException
    {

        ArrayList mailList = new ArrayList();
        mailList.add(mailItem);
        return sendMails(mailList);

    }

    public boolean sendMails(Collection emails) throws MessagingException,
        UnsupportedEncodingException
    {
        Session session = null;
        Transport transport = null;
        int emailCount = emails.size();
        int count = 0;
        int sendFailedCount = 0;
        try {
            for (Iterator emailIt = emails.iterator(); emailIt.hasNext();) {
                count++;
                if ((transport == null) || (session == null)) {
                    Properties props = initProperties();
                    session = Session.getDefaultInstance(props, null);
                    transport = session.getTransport("smtp");
                    transport.connect();

                }

                Mail mail = (Mail)emailIt.next();
                log.info("Sending email: " + mail.toFullString());

                try {

                    MimeMessage message = createMimeMessage(mail, session);
                    if (message == null)
                        continue;

                    transport.sendMessage(message, message.getAllRecipients());

                    // close the connection every other 100 emails
                    if ((count % 100) == 0) {
                        try {
                            if (transport != null)
                                transport.close();
                        } catch (MessagingException ex) {
                            log.error("MessagingException has occured.", ex);
                        }
                        transport = null;
                        session = null;
                    }
                } catch (SendFailedException ex) {
                    sendFailedCount++;
                    log.error("SendFailedException has occured.", ex);
                    log.error("Detailed email information" + mail.toFullString());
                    if (emailCount == 1)
                        throw ex;
                    if ((emailCount != 1) && (sendFailedCount > 10)) {
                        throw ex;
                    }
                } catch (MessagingException mex) {
                    log.error("MessagingException has occured.", mex);
                    log.error("Detailed email information" + mail.toFullString());
                    if (emailCount == 1)
                        throw mex;

                } catch (XWikiException e) {
                    log.error("XWikiException has occured.", e);
                } catch (IOException e) {
                    log.error("IOException has occured.", e);
                }
            }
        } finally {
            try {
                if (transport != null)
                    transport.close();

            } catch (MessagingException ex) {
                log.error("MessagingException has occured.", ex);
            }

            log.info("sendEmails: Email count = " + emailCount + " sent count = " + count);

        }
        return true;
    }

    public int sendMailFromTemplate(String templateDocFullName, String from, String to,
        String cc, String bcc, String language, VelocityContext vcontext) throws XWikiException
    {

        VelocityContext updatedVelocityContext = prepareVelocityContext(from, to, bcc, vcontext);
        XWiki xwiki = context.getWiki();
        XWikiDocument doc = xwiki.getDocument(templateDocFullName, context);
        BaseObject obj = doc.getObject(EMAIL_XWIKI_CLASS_NAME, "language", language);
        if (obj == null)
            obj = doc.getObject(EMAIL_XWIKI_CLASS_NAME, "language", "en");
        if (obj == null) {
            return ERROR_TEMPLATE_EMAIL_OBJECT_NOT_FOUND;
        }
        String subject = obj.getStringValue("subject");
        String txtContent = obj.getStringValue("text");
        String htmlContent = obj.getStringValue("html");

        String evaluatedSubject = XWikiVelocityRenderer.evaluate(subject, templateDocFullName,
                updatedVelocityContext);
        String msg =
            XWikiVelocityRenderer.evaluate(txtContent, templateDocFullName,
                updatedVelocityContext);
        String html =
            XWikiVelocityRenderer.evaluate(htmlContent, templateDocFullName,
                updatedVelocityContext);

        Mail mail = new Mail();
        try {
            mail.setSubject(evaluatedSubject);
            mail.setFrom((String) updatedVelocityContext.get("from.address"));
            mail.setTo((String) updatedVelocityContext.get("to.address"));
            String toBcc = (String) updatedVelocityContext.get("to.bcc");
            if (toBcc != null)
                mail.setBcc(toBcc);
            mail.setTextPart(msg);
            mail.setHtmlPart(html);
            sendMail(mail);
            return 0;
        } catch (Exception e) {
            log.error("sendEmailFromTemplate: " + templateDocFullName + " vcontext: "
                + updatedVelocityContext, e);
            return ERROR;
        }
    }

    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new MailSenderPluginApi((MailSenderPlugin) plugin, context);
    }
    
    protected Log getLogger()
    {
        return log;
    }

}
