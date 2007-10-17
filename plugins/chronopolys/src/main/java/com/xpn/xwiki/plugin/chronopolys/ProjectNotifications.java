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
import com.xpn.xwiki.api.*;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.doc.XWikiDocument;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;

public class ProjectNotifications
{
    public static final String CLASS_NOTIFICATIONS =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "NotificationsClass";

    public static final String DEFAULT_PROJECT_NOTIFICATIONS =
        "PLOG|WIKI|ProjectPhases|ProjectDocuments|WebHome|ProjectMembers";

    public static final String NOTIFICATION_PROJECT_INVITATION =
        "xwiki:ChronoTemplates.ProjectInvitationNotificationEmail";

    Project project;

    public ProjectNotifications(Project project)
    {
        this.project = project;
    }

    public List getSubscribers(String docname, XWikiContext context)
    {
        ArrayList subsribers = new ArrayList();
        try {
            XWikiDocument md = context.getWiki()
                .getDocument(project.getSpace() + "." + Project.PROJECT_MEMBERSDOC, context);
            Vector mo = md.getObjects(CLASS_NOTIFICATIONS);
            if (mo != null) {
                Iterator it = mo.iterator();
                BaseObject obj;

                while (it.hasNext()) {
                    obj = (BaseObject) it.next();
                    if (obj != null) {
                        String notifications = (String) obj.getStringValue("notifications");
                        if (notifications.contains(docname)) {
                            String member = (String) obj.getStringValue("member");
                            subsribers.add(member);
                        }
                    }
                }
            }
        } catch (XWikiException e) {
        }

        return subsribers;
    }

    public List getMembersToNotifyForPlog(XWikiDocument plogdoc, XWikiContext context)
        throws XWikiException
    {
        ArrayList users = new ArrayList();
        if (ProjectLog.PLOGTASK.equals(plogdoc.getStringValue("type"))) {
            users.add(plogdoc.getStringValue("taskassignee").toString());
        }
        if (ProjectLog.PLOGMEETING.equals(plogdoc.getStringValue("type"))) {
            Vector objs = plogdoc.getObjects(ProjectLog.CLASS_PROJECTARTICLERSVP);
            Iterator it = objs.iterator();
            while (it.hasNext()) {
                BaseObject obj = (BaseObject) it.next();
                if (obj != null) {
                    users.add(obj.displayView("member", context));
                }
            }
        }
        return users;
    }

    public List getNotificationsSubscribers(XWikiContext context) throws XWikiException
    {
        XWikiDocument xProjectMembers = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_MEMBERSDOC, context);
        Vector objs = xProjectMembers.getObjects(CLASS_NOTIFICATIONS);
        ArrayList subscribers = new ArrayList();
        if (objs != null) {
            Iterator it = objs.iterator();
            BaseObject obj;

            while (it.hasNext()) {
                obj = (BaseObject) it.next();
                if (obj != null) {
                    subscribers.add(obj.getStringValue("member"));
                }
            }
        }
        return subscribers;
    }

    public com.xpn.xwiki.api.Object getUserNotificationsObj(XWikiContext context)
        throws XWikiException
    {
        XWikiDocument xProjectMembers = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_MEMBERSDOC, context);
        Object notifs =
            this.getUserNotificationsObj(xProjectMembers, context.getLocalUser(), context);
        if (notifs == null) {
            notifs = new Object(xProjectMembers.newObject(CLASS_NOTIFICATIONS, context), context);
            notifs.set("member", context.getLocalUser());
            context.getWiki().saveDocument(xProjectMembers, context);
        }
        return notifs;
    }

    public Object getUserNotificationsObj(XWikiDocument xProjectMembers, String user,
        XWikiContext context) throws XWikiException
    {
        Vector objs = xProjectMembers.getObjects(CLASS_NOTIFICATIONS);
        if (objs != null) {
            Iterator it = objs.iterator();
            BaseObject obj;

            while (it.hasNext()) {
                obj = (BaseObject) it.next();
                if (obj != null) {
                    if (user.equals(obj.getStringValue("member"))) {
                        return new Object(obj, context);
                    }
                }
            }
        }
        Object notifs =
            new Object(xProjectMembers.newObject(CLASS_NOTIFICATIONS, context), context);
        notifs.set("member", user);
        context.getWiki().saveDocument(xProjectMembers, context);
        return notifs;
    }

    public List getUserNotifications(String user, XWikiContext context) throws XWikiException
    {
        XWikiDocument xMembersDoc = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_MEMBERSDOC, context);
        Document membersDoc = new Document(xMembersDoc, context);
        Object notifs = getUserNotificationsObj(xMembersDoc, user, context);
        if (notifs != null) {
            return (List) membersDoc.getValue("notifications", notifs);
        } else {
            return new ArrayList();
        }
    }

    public void setUserNotifications(String user, String items, XWikiContext context)
        throws XWikiException
    {
        XWikiDocument xProjectMembers = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_MEMBERSDOC, context);
        Object notifs = getUserNotificationsObj(xProjectMembers, user, context);

        if (notifs == null) {
            notifs = new Object(xProjectMembers.newObject(CLASS_NOTIFICATIONS, context), context);
            notifs.set("member", user);
        }

        notifs.set("notifications", items);
        context.getWiki().saveDocument(xProjectMembers, context);
    }
}
