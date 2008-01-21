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
import com.xpn.xwiki.api.Api;

import java.util.*;

import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.SortTool;

public class ChronopolysPluginApi extends Api
{
    private ChronopolysPlugin plugin;

    public ChronopolysPluginApi(ChronopolysPlugin plugin, XWikiContext context)
    {
        super(context);
        setPlugin(plugin);
    }

    public ChronopolysPlugin getPlugin()
    {
        return plugin;
    }

    public void setPlugin(ChronopolysPlugin plugin)
    {
        this.plugin = plugin;
    }

    public String version()
    {
        return plugin.getVersion();
    }

    public int getDEADLINE_PHASE() {
        return UserManager.DEADLINE_PHASE;
    }

    public int getDEADLINE_PROJECT() {
        return UserManager.DEADLINE_PROJECT;
    }

    public int getDEADLINE_TASK() {
        return UserManager.DEADLINE_TASK;
    }

    public int getDEADLINE_MEETING() {
        return UserManager.DEADLINE_MEETING;
    }

    /* *********************************************************************************************
     *  Various getters
     */

    public String getProjectContainerClass()
    {
        return plugin.getProjectContainerClass();
    }

    public String getProjectContainerSpace()
    {
        return plugin.getProjectContainerSpace();
    }

    public List getChronoProjectPages()
    {
        return plugin.getChronoProjectPages();
    }

    public String getChronoPreference(String optname) throws XWikiException
    {
        return plugin.getChronoPreference(optname, context);
    }

   /* *********************************************************************************************
    *  Utils
    */

    public List getNewList()
    {
        return plugin.getUtils().getNewList();
    }

    public ListTool getListTool()
    {
        return plugin.getUtils().getListTool();
    }

    public SortTool getSortTool()
    {
        return plugin.getUtils().getSortTool();
    }

    public boolean isDate(String date)
    {
        return plugin.getUtils().isDate(date);
    }

    public String getStaticSkinFile(String filename) throws XWikiException
    {
        return plugin.getUtils().getStaticSkinFile(filename, context);
    }

    public String arrayToString(String[] a, String separator)
    {
        return plugin.getUtils().arrayToString(a, separator);
    }

    public String getRandomAlphanumeric(int length)
    {
        return plugin.getUtils().getRandomAlphanumeric(length);
    }

    /* *********************************************************************************************
     *  User management
     */

    public List getXWikiUsers() throws XWikiException
    {
        return plugin.getUserManager().getXWikiUsers(context);
    }

    public boolean isManager() throws XWikiException
    {
        return plugin.getUserManager().isManager(context);
    }

    public boolean isAdmin() throws XWikiException
    {
        return plugin.getUserManager().isAdmin(context);
    }

    public boolean resetPassword(String user)
    {
        return plugin.getUserManager().resetPassword(user, context);
    }

    public void setLanguage(String user)
    {
        plugin.getUserManager().setLanguage(user, context);
    }

    public String getMyLanguage() throws XWikiException
    {
        return plugin.getUserManager().getUserLanguage(context.getUser(), context);
    }

    public int getMyProjectSubscriptionsNb() throws XWikiException
    {
        return plugin.getUserManager().getMyProjectSubscriptionsNb(context);
    }

    public List getMyProjectSubscriptions() throws XWikiException
    {
        return plugin.getUserManager().getMyProjectSubscriptions(context);
    }

    public int getMyTasksNb() throws XWikiException
    {
        return plugin.getUserManager().getMyTasksNb(context);
    }

    public List getMyTasks(int limit, int start) throws XWikiException
    {
        return plugin.getUserManager().getMyTasks(limit, start, context);
    }

    public int getMyMeetingsNb() throws XWikiException
    {
        return plugin.getUserManager().getMyMeetingsNb(context);
    }

    public List getMyMeetings(int limit, int start) throws XWikiException
    {
        return plugin.getUserManager().getMyMeetings(limit, start, context);
    }

    public int getMyProjectsNb() throws XWikiException
    {
        return plugin.getUserManager().getMyProjectsNb(context);
    }

    public List getMyProjects(int limit, int start) throws XWikiException
    {
        return plugin.getUserManager().getMyProjects(limit, start, context);
    }

    public List getMyLastModifications() throws XWikiException
    {
        return plugin.getUserManager().getMyLastModifications(context);
    }

    public List getMyLastModifications(int limit) throws XWikiException
    {
        return plugin.getUserManager().getMyLastModifications(limit, context);
    }

    public List getMyNextDeadlines() throws XWikiException
    {
        return plugin.getUserManager().getMyNextDeadlines(context);
    }

    public List getMyNextDeadlines(int limit) throws XWikiException
    {
        return plugin.getUserManager().getMyNextDeadlines(limit, context);
    }   

    /* *********************************************************************************************
     *  Project management
     */

    public boolean isProject(String uid) throws XWikiException
    {
        return plugin.getProjectManager().projectExists(uid, context);
    }

    public ProjectApi addProject(String name) throws XWikiException
    {
        return plugin.getProjectManager().addProject(name, context);
    }

    public ProjectApi getProject(String name) throws XWikiException
    {
        return plugin.getProjectManager().getProject(name, context);
    }

    public List getProjects() throws XWikiException
    {
        return plugin.getProjectManager().getProjects(context);
    }

    /* *********************************************************************************************
     *  Folders management
     */

    public String addProjectContainer(String type, String parent) throws XWikiException
    {
        return plugin.getFolderManager().addProjectContainer(type, parent, context);
    }

    public String addProjectContainer(String type, String parent, String name, String desc,
        String style) throws XWikiException
    {
        return plugin.getFolderManager()
            .addProjectContainer(type, parent, name, desc, style, context);
    }

    public Object getProjectContainer(String uid) throws XWikiException
    {
        return plugin.getFolderManager().getProjectContainer(uid, context);
    }

    public void deleteProjectContainer(String uid) throws XWikiException
    {
        plugin.getFolderManager().deleteProjectContainer(uid, true, context);
    }

    public List getProjectContainerChilds(String uid) throws XWikiException
    {
        return plugin.getFolderManager().getProjectContainerChilds(uid, context);
    }

    public List getProjectContainerProjects(String uid) throws XWikiException
    {
        return plugin.getFolderManager().getProjectContainerProjects(uid, context);
    }

    public int getProjectContainerProjectsNumber(String uid) throws XWikiException
    {
        return plugin.getFolderManager().getProjectContainerProjectsNumber(uid, context);
    }

    public String moveProjectContainerUp(String uid) throws XWikiException
    {
        return plugin.getFolderManager().moveProjectContainer(uid, true, context);
    }

    public String moveProjectContainerDown(String uid) throws XWikiException
    {
        return plugin.getFolderManager().moveProjectContainer(uid, false, context);
    }

    public String getLastProjectContainerIndex(String parent) throws XWikiException
    {
        return String
            .valueOf(plugin.getFolderManager().getLastProjectContainerIndex(parent, context));
    }

    public String getProjectContainerStyle(String uid) throws XWikiException
    {
        return plugin.getFolderManager().getProjectContainerStyle(uid, context);
    }

    public List getAllProjectContainers() throws XWikiException
    {
        return plugin.getFolderManager().getProjectContainers(context);
    }

    public List getProjectContainers(String type) throws XWikiException
    {
        return plugin.getFolderManager().getRootFolders(context);
    }
}
