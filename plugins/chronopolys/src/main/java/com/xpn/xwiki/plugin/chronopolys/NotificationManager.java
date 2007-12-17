/*
 * Copyright 2006-2007, AVANE sarl, and individual contributors.
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
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.notify.DocChangeRule;
import com.xpn.xwiki.notify.XWikiActionNotificationInterface;
import com.xpn.xwiki.notify.XWikiActionRule;
import com.xpn.xwiki.notify.XWikiDocChangeNotificationInterface;
import com.xpn.xwiki.notify.XWikiNotificationInterface;
import com.xpn.xwiki.notify.XWikiNotificationRule;
import com.xpn.xwiki.plugin.mailsender.MailSenderPlugin;
import com.xpn.xwiki.plugin.mailsender.MailSenderPluginApi;
import com.xpn.xwiki.plugin.watchlist.WatchListPlugin;
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
        NOTIFICATION_SPACE + "." + "ProjectActivation" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_TASK_ASSIGNMENT =
        NOTIFICATION_SPACE + "." + "TaskAssignment" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_TASK_COMPLETION =
        NOTIFICATION_SPACE + "." + "TaskCompleted" + NOTIFICATION_SUFFIX;

    public static final String NOTIFICATION_MEETING_INVITATION =
        NOTIFICATION_SPACE + "." + "MeetingInvatation" + NOTIFICATION_SUFFIX;

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
     *      com.xpn.xwiki.doc.XWikiDocument,com.xpn.xwiki.doc.XWikiDocument,
     *      int,com.xpn.xwiki.XWikiContext)
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

                    /* Flush projet's lastmodifications cache */
                    if (plugin.getProjectManager().getProjectsCache() != null) {
                        plugin.getProjectManager().getProjectsCache()
                            .flushEntry(newdoc.getSpace() + "-lastmodifications");
                    }

                    if (project.get("status").equals("1")) {

                        if (newdoc != null && olddoc != null && Project.PROJECT_HOMEDOC.equals(newdoc.getName())
                            && !newdoc.getStringValue("status")
                            .equals(olddoc.getStringValue("status")))
                        {
                            /* Project status has changed */
                            rcpt = project.getMembers();
                            if (newdoc.getStringValue("status").equals("1")) {
                                /* Project just get active */
                                mailTemplate = NOTIFICATION_ACTIVE_PROJECT;
                                /* Add project space to member's watchlist */
                                WatchListPlugin wlplugin = (WatchListPlugin)context.getWiki().getPlugin("watchlist", context);
                                Iterator it = rcpt.iterator();
                                while (it.hasNext()) {
                                    wlplugin.addWatchedElement((String)it.next(), project.getSpace(), true, context);
                                }
                            }
                        } else if (newdoc != null && project.isPlogPage(newdoc.getName())) {
                            /* PLOG */
                            if (project.isPlogTask(newdoc)) {
                                if (olddoc != null && !newdoc.getStringValue("taskassignee")
                                    .equals(olddoc.getStringValue("taskassignee")))
                                {
                                    /* New Assignee */
                                    rcpt = new ArrayList();
                                    rcpt.add(newdoc.getStringValue("taskassignee"));
                                    mailTemplate = NOTIFICATION_TASK_ASSIGNMENT;
                                } else if (newdoc.getStringValue("taskcompletion").equals("100%")) {
                                    /* Completed task */
                                    rcpt = new ArrayList();
                                    rcpt.add(newdoc.getCreator());
                                    mailTemplate = NOTIFICATION_TASK_COMPLETION;
                                }
                            }
                        }
                        if (rcpt.size() > 0 && !mailTemplate.equals("")) {
                            if (!context.getWiki().exists(mailTemplate, context)) {
                                return;
                            }
                            /* Send notifications */
                            this.sendNotification(rcpt, project, new Document(newdoc, context), mailTemplate, context);
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
     *      com.xpn.xwiki.doc.XWikiDocument,java.lang.String,com.xpn.xwiki.XWikiContext)
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
    public void sendNotification(List rcpt, ProjectApi project, Document newdoc,
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
        vcontext.put("displaytitle", newdoc.getDisplayTitle());
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
            vcontext.put("username", context.getWiki().getLocalUserName(user, null, false, context));
            emailService.sendMessageFromTemplate(
                plugin.getChronoPreference("notifications_sender", context),
                email, null, null, language, mailTemplate, vcontext);
        }
    }
}
