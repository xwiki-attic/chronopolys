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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;

public class ChronopolysPlugin extends XWikiDefaultPlugin
{
    public static final String CLASS_DEFAULT_SPACE = "ChronoClasses";

    public static final String CLASS_XWIKIUSERS = "XWiki.XWikiUsers";

    public static final String CLASS_CHRONOPREFS = "ChronoAdmin.ChronoPreferences";

    public static final String CHRONOPROJECT_VERSION = "0.1";

    public static final String SEP = ".";

    private ProjectManager projectManager;

    private FolderManager folderManager;

    private NotificationManager notificationManager;

    private UserManager userManager;

    private Utils utils;

    /*
     * Constructor
     */
    public ChronopolysPlugin(String name, String className, XWikiContext context)
        throws XWikiException
    {
        super(name, className, context);
        this.projectManager = new ProjectManager(this);
        this.folderManager = new FolderManager(this);
        this.notificationManager = new NotificationManager(this, context);
        this.userManager = new UserManager(this);
        this.utils = new Utils();
    }

    /*
     * Instances getters
     */
    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public FolderManager getFolderManager()
    {
        return folderManager;
    }

    public NotificationManager getNotificationManager()
    {
        return notificationManager;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public Utils getUtils()
    {
        return utils;
    }

    /*
     * Miscellaneous getters
     */
    public String getProjectContainerClass()
    {
        return FolderManager.CLASS_FOLDER;
    }

    public String getProjectContainerSpace()
    {
        return FolderManager.FOLDERS_SPACE;
    }

    public String getVersion()
    {
        return CHRONOPROJECT_VERSION;
    }

    public List getChronoProjectPages()
    {
        return Arrays.asList(Project.DEFAULT_PROJECT_DOCS.split(","));
    }

    /*
     * Get the plugin's name
     *
     * @return name of the plugin
     */
    public String getName()
    {
        return "chronopolys";
    }

    /*
     * Get the plugin API
     */
    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new ChronopolysPluginApi((ChronopolysPlugin) plugin, context);
    }

    /*
     * Flush all the chronopolys caches
     */
    public void flushCache(XWikiContext context)
    {
        this.getUserManager().flushUsersCache();
        this.getUserManager().flushUserdataCache();
        this.getProjectManager().flushProjectsCache();
        this.getFolderManager().flushFoldersCache();
    }

    /*
     * Plugin initialization
     */
    public void init(XWikiContext context)
    {
        super.init(context);
    }

    /*
     * Plugin initialization (virtual mode)
     */
    public void virtualInit(XWikiContext context)
    {
        super.virtualInit(context);
    }

    /*
     * Get a chronopolys preference
     */
    public String getChronoPreference(String optname, XWikiContext context) throws XWikiException
    {
        return context.getWiki().getDocument(CLASS_CHRONOPREFS, context)
            .getObject(CLASS_CHRONOPREFS).getStringValue(optname);
    }
}