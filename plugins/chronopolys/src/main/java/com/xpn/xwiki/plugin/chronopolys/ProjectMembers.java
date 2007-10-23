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
import com.xpn.xwiki.api.*;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.doc.XWikiDocument;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class ProjectMembers
{
    public static final String CLASS_MEMBERSHIP = "XWiki.XWikiGroups";

    public static final int PROJECT_PRIVATE_RIGHTS = 0;

    Project project;

    public ProjectMembers(Project project)
    {
        this.project = project;
    }

    public Document getMemberstDoc(XWikiContext context) throws XWikiException {
        return context.getWiki()
            .getDocument(project.getSpace(), Project.PROJECT_MEMBERSDOC, context)
            .newDocument(context);
    }

    public void setProjectRights(int mode, XWikiContext context) throws XWikiException
    {
        String space = project.getSpace();
        XWikiDocument prefs =
            context.getWiki().getDocument(space + "." + "WebPreferences", context);
        /* first prefs global rights entry : project administrators */
        BaseObject grights = prefs.getObject("XWiki.XWikiGlobalRights", 0);
        grights.set("groups", "ChronoAdmin.AdminGroup,ChronoAdmin.ManagerGroup", context);
        grights.set("levels", "view,edit,delete", context);
        grights.setIntValue("allow", 1);
        /* second prefs global rights entry : project members */
        BaseObject lrights = prefs.getObject("XWiki.XWikiGlobalRights", 1);
        lrights.set("groups", space + "." + Project.PROJECT_MEMBERSDOC, context);
        lrights.set("levels", "view,edit", context);
        lrights.setIntValue("allow", 1);
        /* third prefs global rights entry : project guests */
        BaseObject gurights = prefs.getObject("XWiki.XWikiGlobalRights", 2);
        gurights.set("groups", space + "." + Project.PROJECT_GUESTSDOC, context);
        gurights.set("levels", "view", context);
        gurights.setIntValue("allow", 1);
        context.getWiki().saveDocument(prefs, context);
        /* Note */
        XWikiDocument note =
            context.getWiki().getDocument(space + "." + Project.PROJECT_NOTEDOC, context);
        BaseObject nrights = note.getObject("XWiki.XWikiRights", 0);
        nrights.set("groups", space + "." + Project.PROJECT_GUESTSDOC + "," +
            space + "." + Project.PROJECT_MEMBERSDOC, context);
        nrights.set("levels", "view,edit", context);
        nrights.setIntValue("allow", 1);
        context.getWiki().saveDocument(note, context);

        // TODO insert votes rights here
        /* first & second projetLeaders entry : project creator */
        XWikiDocument leaders =
            context.getWiki().getDocument(space + "." + Project.PROJECT_LEADERSDOC, context);
        BaseObject plrights = leaders.getObject("XWiki.XWikiGroups", 0);
        plrights.set("member", context.getLocalUser(), context);
        plrights = leaders.getObject("XWiki.XWikiGroups", 1);
        plrights.set("member", context.getLocalUser(), context);
        context.getWiki().saveDocument(leaders, context);
        /* projectHome rights entry : project administrators */
        XWikiDocument home = context.getWiki().getDocument(space + "." + "WebHome", context);
        BaseObject prights = home.getObject("XWiki.XWikiRights", 0);
        prights.set("groups", space + "." + Project.PROJECT_LEADERSDOC +
            ",ChronoAdmin.AdminGroup,ChronoAdmin.ManagerGroup", context);
        prights.set("levels", "admin,edit", context);
        prights.setIntValue("allow", 1);
        context.getWiki().saveDocument(home, context);
        /* projectMembers rights entry : project administrators */
        XWikiDocument members =
            context.getWiki().getDocument(space + "." + Project.PROJECT_MEMBERSDOC, context);
        BaseObject mrights = members.getObject("XWiki.XWikiRights", 0);
        mrights.set("groups", space + "." + Project.PROJECT_LEADERSDOC +
            ",ChronoAdmin.AdminGroup,ChronoAdmin.ManagerGroup", context);
        mrights.set("levels", "admin,edit", context);
        mrights.setIntValue("allow", 1);
        context.getWiki().saveDocument(members, context);
        /* projectPhases rights entry : project administrators */
        XWikiDocument phases =
            context.getWiki().getDocument(space + "." + Project.PROJECT_PHASESDOC, context);
        BaseObject phrights = phases.getObject("XWiki.XWikiRights", 0);
        phrights.set("groups", space + "." + Project.PROJECT_LEADERSDOC +
            ",ChronoAdmin.AdminGroup,ChronoAdmin.ManagerGroup", context);
        phrights.set("levels", "admin,edit", context);
        phrights.setIntValue("allow", 1);
        context.getWiki().saveDocument(phases, context);
    }

    public String getProjectCreator(XWikiContext context) throws XWikiException
    {
        XWikiDocument leadersDoc = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_LEADERSDOC, context);
        return leadersDoc.getObject("XWiki.XWikiGroups", 1).displayView("member", context);
    }

    public void setProjectCreator(String member, XWikiContext context) throws XWikiException
    {
        XWikiDocument leadersDoc = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_LEADERSDOC, context);
        leadersDoc.getObject("XWiki.XWikiGroups", 1).set("member", member, context);
        leadersDoc.setComment("setprojectcreator|" + member);
        context.getWiki().saveDocument(leadersDoc, context);
    }

    public String getProjectLeader(XWikiContext context) throws XWikiException
    {
        XWikiDocument leadersDoc = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_LEADERSDOC, context);
        return leadersDoc.getObject("XWiki.XWikiGroups", 0).displayView("member", context);
    }

    public void setProjectLeader(String member, XWikiContext context) throws XWikiException
    {
        XWikiDocument leadersDoc = context.getWiki()
            .getDocument(project.getSpace() + "." + Project.PROJECT_LEADERSDOC, context);
        leadersDoc.getObject("XWiki.XWikiGroups", 0).set("member", member, context);
        leadersDoc.setComment("setprojectleader|" + member);
        context.getWiki().saveDocument(leadersDoc, context);
    }

    public boolean addMember(String name, XWikiContext context) throws XWikiException
    {
        if (isMember(name, context)) {
            return false;
        }

        /* create basic membership */
        XWikiDocument projectMembersXWiki =
            context.getWiki().getDocument(project.getSpace(), Project.PROJECT_MEMBERSDOC, context);
        Document projectMembers = new Document(projectMembersXWiki, context);
        int nb = projectMembers.createNewObject("XWiki.XWikiGroups");
        com.xpn.xwiki.api.Object obj = projectMembers.getObject("XWiki.XWikiGroups", nb);
        obj.set("member", name);
        /* get 'ProjectMembers' notification subscribers before the new member become one of them */
        List rcpt = project.getNotifications().getSubscribers("ProjectMembers", context);
        /* create members notifications' object */
        nb = projectMembers.createNewObject(ProjectNotifications.CLASS_NOTIFICATIONS);
        obj = projectMembers.getObject(ProjectNotifications.CLASS_NOTIFICATIONS, nb);
        obj.set("member", name);
        obj.set("notifications", ProjectNotifications.DEFAULT_PROJECT_NOTIFICATIONS);
        projectMembers.save("addmember|" + name);

        // invalidate ChronopolysPlugin caches
        project.getPlugin().getProjectManager().flushProjectsCache();
        project.getPlugin().getFolderManager().flushFoldersCache();
        project.getPlugin().getUserManager().flushUserdataCache();

        // email notification (if the project is active)
        if (project.get("status").equals("1")) {
            /* notify new member */
            List newmember = new ArrayList();
            newmember.add(name);
            project.getPlugin().getNotificationManager()
                .sendNotification(newmember, new ProjectApi(project, context),
                    context.getWiki().getDocument(name, context),
                    ProjectNotifications.NOTIFICATION_PROJECT_INVITATION, context);
            /* notify subscribers */
            project.getPlugin().getNotificationManager()
                .sendNotification(rcpt, new ProjectApi(project, context),
                    context.getWiki().getDocument(name, context),
                    NotificationManager.NOTIFICATION_MEMBER, context);
        }

        return true;
    }

    public boolean isMember(String docName, XWikiContext context) throws XWikiException
    {
        List users = getMembers(context);
        return users.contains(docName);
    }

    public List getMembers(XWikiContext context) throws XWikiException
    {
        XWikiDocument projectMembersXWiki =
            context.getWiki().getDocument(project.getSpace(), Project.PROJECT_MEMBERSDOC, context);
        Document projectMembers = new Document(projectMembersXWiki, context);
        List objs = projectMembers.getObjects("XWiki.XWikiGroups");
        Iterator it = objs.iterator();
        List members = new ArrayList();
        while (it.hasNext()) {
            members.add(((Object) it.next()).display("member", "view"));
        }

        return members;
    }

    // TODO : remove members entries in the project (meetings, tasks, etc) ?
    public boolean removeMember(String member, XWikiContext context) throws XWikiException
    {
        if (!isMember(member, context)) {
            return false;
        }
        XWikiDocument projectMembersXWiki =
            context.getWiki().getDocument(project.getSpace(), Project.PROJECT_MEMBERSDOC, context);
        Document projectMembers = new Document(projectMembersXWiki, context);

        // Revoke membership
        Vector objs = projectMembers.getObjects(CLASS_MEMBERSHIP);
        Object obj;
        Iterator it = objs.iterator();
        while (it.hasNext()) {
            obj = (Object) it.next();
            if (obj.get("member").equals(member)) {
                projectMembers.removeObject(obj);
            }
        }

        // Revoke notifications
        objs = projectMembers.getObjects(ProjectNotifications.CLASS_NOTIFICATIONS);
        it = objs.iterator();
        while (it.hasNext()) {
            obj = (Object) it.next();
            if (obj.get("member").equals(member)) {
                projectMembers.removeObject(obj);
            }
        }

        projectMembers.save("removemember|" + member);

        // invalidate ChronopolysPlugin caches
        project.getPlugin().getProjectManager().flushProjectsCache();
        project.getPlugin().getFolderManager().flushFoldersCache();

        return true;
    }

    public Object getUserMembership(String user, XWikiContext context) throws XWikiException
    {
        return this.getUserMembership(context.getWiki().getDocument(
            project.getSpace() + "." + Project.PROJECT_MEMBERSDOC, context), user, context);
    }

    public Object getUserMembership(XWikiDocument projectMembers, String user, XWikiContext context)
        throws XWikiException
    {
        Vector objs = projectMembers.getObjects("XWiki.XWikiGroups");
        Iterator it = objs.iterator();
        BaseObject obj;
        while (it.hasNext()) {
            obj = (BaseObject) it.next();
            if (user.equals(obj.get("member"))) {
                return new Object(obj, context);
            }
        }
        return null;
    }
}
