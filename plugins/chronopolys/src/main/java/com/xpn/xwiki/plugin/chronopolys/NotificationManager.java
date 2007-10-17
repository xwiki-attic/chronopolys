/*
 * Copyright 2006-2007, Avane SARL, and individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * @author jvdrean
 */
package com.xpn.xwiki.plugin.chronopolys;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.notify.*;
import com.xpn.xwiki.plugin.mailsender.MailSenderPlugin;
import com.xpn.xwiki.plugin.mailsender.MailSenderPluginApi;
import com.xpn.xwiki.render.XWikiVelocityRenderer;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager
    implements XWikiDocChangeNotificationInterface, XWikiActionNotificationInterface
{
    public static final String CLASS_NOTIFICATIONS =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "NotificationsClass";

    public static final String NOTIFICATION_DB = "xwiki";

    public static final String NOTIFICATION_SPACE = NOTIFICATION_DB + ":" + "ChronoTemplates";

    public static final String NOTIFICATION_SUFFIX = "NotificationEmail";

    public static final String NOTIFICATION_ACTIVE_PROJECT =
        NOTIFICATION_SPACE + "." + "ProjectActivationNotificationEmail";

    public static final String NOTIFICATION_PLOGTASK =
        NOTIFICATION_SPACE + "." + "PlogTask" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_PLOGMEETING =
        NOTIFICATION_SPACE + "." + "PlogMeeting" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_PLOGPOST =
        NOTIFICATION_SPACE + "." + "PlogPost" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_WIKI =
        NOTIFICATION_SPACE + "." + "Wiki" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_MEMBER =
        NOTIFICATION_SPACE + "." + "Member" + NOTIFICATION_SUFFIX;

    private ChronopolysPlugin plugin;

    /*
     * Constructor
     */
    public NotificationManager(ChronopolysPlugin plugin, XWikiContext context)
    {
        this.plugin = plugin;
        context.getWiki().getNotificationManager().addGeneralRule(new DocChangeRule(this));
        context.getWiki().getNotificationManager().addGeneralRule(new XWikiActionRule(this));
    }

    /**
     * @see com.xpn.xwiki.notify.XWikiDocChangeNotificationInterface#notify(com.xpn.xwiki.notify.XWikiNotificationRule,
     *com.xpn.xwiki.doc.XWikiDocument,com.xpn.xwiki.doc.XWikiDocument,
     *int,com.xpn.xwiki.XWikiContext)
     */
    public void notify(final XWikiNotificationRule rule, final XWikiDocument newdoc,
        final XWikiDocument olddoc, final int event, final XWikiContext context)
    {
        List rcpt = new ArrayList();
        String mailTemplate = "";

        // folders
        if (newdoc.getObject(FolderManager.CLASS_FOLDER) != null) {
            plugin.getFolderManager().flushFoldersCache();
        }

        if (event == XWikiNotificationInterface.EVENT_NEW ||
            event == XWikiNotificationInterface.EVENT_DELETE)
        {
            // chronopolys user
            if ((newdoc.getObject(ChronopolysPlugin.CLASS_XWIKIUSERS) != null ||
                olddoc.getObject(ChronopolysPlugin.CLASS_XWIKIUSERS) != null))
            {
                plugin.getUserManager().flushUsersCache();
            }
            // meeting or task
            if ((newdoc.getObjects(ProjectLog.CLASS_PROJECTARTICLE) != null) ||
                (olddoc.getObjects(ProjectLog.CLASS_PROJECTARTICLE) != null))
            {
                plugin.getUserManager().flushUserdataCache();
            }
            // subscriptions
            if ((newdoc.getObjects(CLASS_NOTIFICATIONS) != null) ||
                (olddoc.getObjects(CLASS_NOTIFICATIONS) != null))
            {
                plugin.getUserManager().flushUserdataCache();
            }
        }

        // Send notifications
        try {
            if (plugin.getProjectManager().isProject(newdoc.getSpace(), context)) {
                ProjectApi project =
                    plugin.getProjectManager().getProject(newdoc.getSpace(), context);
                if (project != null) {

                    if (Project.PROJECT_HOMEDOC.equals(newdoc.getName())) {
                        // This should work but olddoc and newdoc projects objects are always the same :/
                        /* if (!newdoc.getStringValue("status").equals(olddoc.getStringValue("status"))
                     || !newdoc.getStringValue("name").equals(olddoc.getStringValue("name"))
                     || !newdoc.getStringValue("container").equals(olddoc.getStringValue("container"))) { */
                        plugin.getFolderManager().flushFoldersCache();
                        plugin.getProjectManager().flushProjectsCache();
                        plugin.getUserManager().flushUserdataCache();
                        /* } */
                    }

                    if (project.get("status").equals("1")) {

                        if (Project.PROJECT_HOMEDOC.equals(newdoc.getName())
                            && !newdoc.getStringValue("status")
                            .equals(olddoc.getStringValue("status")))
                        {
                            //* Project status has changed *
                            rcpt = project.getMembers();
                            if (newdoc.getStringValue("status").equals("1")) {
                                /* Project just get active */
                                mailTemplate = NOTIFICATION_ACTIVE_PROJECT;
                            }
                        } else if (project.isWikiPage(newdoc.getName())) {
                            /* Wiki */
                            rcpt = project.getSubscribers("WIKI");
                            mailTemplate = NOTIFICATION_WIKI;
                        } else if (project.isPlogPage(newdoc.getName())) {
                            /* PLOG */
                            if (project.isPlogTask(newdoc)) {
                                /* Task */
                                rcpt = project.getMembersToNotifyForPlog(newdoc);
                                mailTemplate = NOTIFICATION_PLOGTASK;
                            } else if (project.isPlogMeeting(newdoc)) {
                                /* Meeting */
                                rcpt = project.getMembersToNotifyForPlog(newdoc);
                                mailTemplate = NOTIFICATION_PLOGMEETING;
                                // TODO : filter
                            } else {
                                /* Post */
                                rcpt = project.getSubscribers("PLOG");
                                mailTemplate = NOTIFICATION_PLOGPOST;
                            }
                        } else {
                            /* All the project default pages (ProjectMembers,ProjectDocuments,etc) */
                            rcpt = project.getSubscribers(newdoc.getName());
                            mailTemplate =
                                NOTIFICATION_SPACE + "." + newdoc.getName() + NOTIFICATION_SUFFIX;
                        }
                        if (rcpt.size() > 0 && !mailTemplate.equals("")) {
                            if (!context.getWiki().exists(mailTemplate, context)) {
                                // mLogger.info("Chronopolys notififications : mailTemplate <" + mailTemplate + "> does not exist");
                                // mLogger.info("Chronopolys notififications : users : <" + rcpt.toString() + ">");
                                return;
                            }
                            /* Send notifications */
                            this.sendNotification(rcpt, project, newdoc, mailTemplate, context);
                        }
                    }
                }
            }
        } catch (XWikiException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see com.xpn.xwiki.notify.XWikiActionNotificationInterface#notify(com.xpn.xwiki.notify.XWikiNotificationRule,
     *com.xpn.xwiki.doc.XWikiDocument,java.lang.String,com.xpn.xwiki.XWikiContext)
     */
    public void notify(final XWikiNotificationRule rule, final XWikiDocument doc,
        final String action,
        final XWikiContext context)
    {

        // Invalidate chronopolys caches
        if ("objectadd".equals(action) || "objectremove".equals(action)) {
            if (doc.getName().equals(Project.PROJECT_MEMBERSDOC)) {
                plugin.getFolderManager().flushFoldersCache();
                plugin.getProjectManager().flushProjectsCache();
            }
            try {
                if (plugin.getProjectManager().isProject(doc.getSpace(), context)) {
                    ProjectApi project =
                        plugin.getProjectManager().getProject(doc.getSpace(), context);
                    if (project.isPlogMeeting(doc)) {
                        plugin.getUserManager().flushUserdataCache();
                    }
                }
            } catch (XWikiException e) {
            }
        }
        // New project document
        // check if the project is active
        if ("upload".equals(action)) {
            try {
                if (plugin.getProjectManager().isProject(doc.getSpace(), context)) {
                    ProjectApi project =
                        plugin.getProjectManager().getProject(doc.getSpace(), context);
                    List rcpt = project.getSubscribers(doc.getName());
                    String mailTemplate =
                        NOTIFICATION_SPACE + "." + doc.getName() + NOTIFICATION_SUFFIX;
                    if (rcpt.size() > 0 && !mailTemplate.equals("")) {
                        this.sendNotification(rcpt, project, doc, mailTemplate, context);
                    }
                }
            } catch (XWikiException e) {
            }
        }
    }

    /*
     * Send a notification message to the given users
     *
     * @param rcpt recipients list
     * @param project chronopolys project
     * @param newdoc notification's source
     * @param mailTemplate wiki page to use as mail template
     * @param context request context
     */
    public void sendNotification(List rcpt, ProjectApi project, XWikiDocument newdoc,
        String mailTemplate, XWikiContext context) throws XWikiException
    {
        MailSenderPluginApi emailService =
            (MailSenderPluginApi) context.getWiki().getPluginApi(MailSenderPlugin.ID, context);
        if (emailService == null) {
            return;
        }
        /* prepare email velocity context */
        VelocityContext vcontext = new VelocityContext();
        vcontext.put("xwiki", new com.xpn.xwiki.api.XWiki(context.getWiki(), context));
        vcontext.put("project", project);
        vcontext.put("projecturl", project.getURL());
        vcontext.put("doc", newdoc);
        vcontext
            .put("docurl", context.getWiki().getExternalURL(newdoc.getFullName(), "view", context));
        vcontext.put("displaytitle", newdoc.getDisplayTitle(context));
        vcontext.put("isNew", project.isPlogNew(newdoc));
        String rawHeader = context.getWiki()
            .getDocument("xwiki:ChronoTemplates.HTMLEmailHeader", context).getContent();
        String rawFooter = context.getWiki()
            .getDocument("xwiki:ChronoTemplates.HTMLEmailFooter", context).getContent();
        vcontext.put("htmlheader", XWikiVelocityRenderer.evaluate(rawHeader, "header", vcontext));
        vcontext.put("htmlfooter", XWikiVelocityRenderer.evaluate(rawFooter, "footer", vcontext));

        /* send email to each recipient */
        Iterator it = rcpt.iterator();
        while (it.hasNext()) {
            String user = (String) it.next();
            String email = plugin.getUserManager().getUserEmail(user, context);
            String language = plugin.getUserManager().getUserLanguage(user, context);
            emailService.sendMessageFromTemplate(
                plugin.getChronoPreference("notifications_sender", context),
                email, null, null, language, mailTemplate, vcontext);
        }
    }
}
