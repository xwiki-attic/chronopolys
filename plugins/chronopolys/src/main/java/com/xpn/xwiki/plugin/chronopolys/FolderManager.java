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
import com.xpn.xwiki.cache.api.XWikiCacheNeedsRefreshException;
import com.xpn.xwiki.cache.api.XWikiCache;
import com.xpn.xwiki.cache.api.XWikiCacheService;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiMessageTool;

import java.util.*;

public class FolderManager
{
    public static final String CLASS_FOLDER =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "ProjectContainerClass";

    public static final String FOLDERS_SPACE = "ProjectContainers";

    public static final String FOLDERS_ROOT = "axis";

    public static final String FOLDERS_LEAF = "activity";

    protected XWikiCache foldersCache;

    private static final int foldersCacheCapacity = 500;

    private ChronopolysPlugin plugin;

    /*
     * Constructor
     */
    public FolderManager(ChronopolysPlugin plugin)
    {
        this.plugin = plugin;
    }

    public ChronopolysPlugin getPlugin()
    {
        return plugin;
    }

    /*
     * Init folders cache
     */
    private synchronized void initFoldersCache(XWikiContext context) throws XWikiException
    {
        if (foldersCache == null) {
            XWikiCacheService cacheService = context.getWiki().getCacheService();
            foldersCache =
                cacheService.newCache("xwiki.chronopolys.folders.cache", foldersCacheCapacity);
        }
    }

    /*
     * Flush folders cache (contains user's folders lists)
     */
    public void flushFoldersCache()
    {
        if (foldersCache != null) {
            foldersCache.flushAll();
            foldersCache = null;
        }
    }

    /*
     * Add a new root folder
     */
    public String addProjectContainer(XWikiContext context) throws XWikiException
    {
        return addProjectContainer(FOLDERS_ROOT, "", context);
    }

    /*
     * Add a new folder (the simpliest way)
     */
    public String addProjectContainer(String type, String parent, XWikiContext context)
        throws XWikiException
    {
        XWikiMessageTool msg = context.getMessageTool();
        String name = msg.get("new" + type);
        String style = msg.get("defaultcontainercolor");
        String desc = msg.get("defaultcontainerdesc");
        return addProjectContainer(type, parent, name, desc, style, context);
    }

    /*
     * Add a new folder
     */
    public String addProjectContainer(String type, String parent, String name, String desc,
        String style, XWikiContext context) throws XWikiException
    {
        String uid = getNewProjectContainerUid(context);
        XWikiDocument containerDoc =
            context.getWiki().getDocument(FOLDERS_SPACE + "." + uid, context);
        BaseObject obj = containerDoc.newObject(CLASS_FOLDER, context);
        obj.set("uid", uid, context);
        obj.set("name", name, context);
        obj.set("style", style, context);
        obj.set("desc", desc, context);
        obj.set("parent", parent, context);
        containerDoc.setParent(parent);
        containerDoc.setCreator(context.getUser());
        if (type == null || type.equals("")) {
            obj.set("type", FOLDERS_ROOT, context);
        } else {
            obj.set("type", type, context);
        }
        int indexMax = getLastProjectContainerIndex(parent, context) + 1;
        obj.set("order", indexMax, context);
        context.getWiki().saveDocument(containerDoc, context);

        this.flushFoldersCache();
        return uid;
    }

    /*
     * Get a new folder ID (unique)
     */
    public String getNewProjectContainerUid(XWikiContext context) throws XWikiException
    {
        String uid = org.apache.commons.lang.RandomStringUtils.randomNumeric(8);
        while (isProjectContainer(uid, context)) {
            uid = org.apache.commons.lang.RandomStringUtils.randomNumeric(8);
        }
        return uid;
    }

    /*
     * Is the page a folder ?
     */
    public boolean isProjectContainer(String uid, XWikiContext context) throws XWikiException
    {
        try {
            new Folder(context.getWiki().getDocument(FOLDERS_SPACE + "." + uid, context), context);
        } catch (ChronopolysPluginException e) {
            return false;
        }
        return true;
    }

    /*
     * Get a folder by ID
     */
    public Folder getProjectContainer(String uid, XWikiContext context) throws XWikiException
    {
        String key = context.getDatabase() + ":" + uid;
        Folder container = null;

        initFoldersCache(context);
        synchronized (key) {
            try {
                // retrieve it from cache
                container = (Folder) foldersCache.getFromCache(key);
            } catch (XWikiCacheNeedsRefreshException e) {
                foldersCache.cancelUpdate(key);
                container =
                    new Folder(context.getWiki().getDocument(FOLDERS_SPACE + "." + uid, context),
                        context);
                foldersCache.putInCache(key, container);
            }
        }
        return container;
    }

    /*
     * Delete a folder
     */
    public void deleteProjectContainer(String uid, boolean firstLevel, XWikiContext context)
        throws XWikiException
    {
        Folder container = getProjectContainer(uid, context);

        if (firstLevel) {
            /* manage container's brothers order */
            ArrayList<Folder> broContainers = getProjectContainerBrothers(uid, true, context);

            Folder currentBroContainer;
            for (int i = 0; i < broContainers.size(); i++) {
                broContainers.get(i).decrementOrder(context);
            }
        }

        if (FOLDERS_LEAF.equals(container.getType())) {
            /* set child projects (if any) as orphans */
            ArrayList<Object> childProjects = getProjectContainerProjects(uid, context);
            if (childProjects != null) {
                for (int j = 0; j < childProjects.size(); j++) {
                    Object pobj = childProjects.get(j);
                    XWikiDocument pdoc = context.getWiki().getDocument(pobj.getName(), context);
                    pdoc.setStringValue(Project.CLASS_PROJECT, "container",
                        Project.PROJECT_DEFAULT_CONTAINER);
                    context.getWiki().saveDocument(pdoc, context);
                }
            }
        }

        /* delete childs */
        ArrayList<Folder> childContainers = getProjectContainerChilds(uid, context);
        if (childContainers != null) {
            for (int i = 0; i < childContainers.size(); i++) {
                this.deleteProjectContainer(childContainers.get(i).getUid(), false, context);
            }
        }

        /* delete container itself */
        XWikiDocument doc = context.getWiki().getDocument(container.getFullName(), context);
        context.getWiki().deleteDocument(doc, context);

        /* unvalidate cache */
        this.flushFoldersCache();
        this.getPlugin().getUserManager().flushUserdataCache();
        this.getPlugin().getProjectManager().flushProjectsCache();
    }

    /*
     * Get the folder's elder or younger brothers
     *
     * @param uid unique ID
     * @param elder if true returns the elder brothers, if false the younger ones
     * @return brothersList
     */
    public ArrayList<Folder> getProjectContainerBrothers(String uid, boolean elder,
        XWikiContext context) throws XWikiException
    {
        Folder container = getProjectContainer(uid, context);
        ArrayList<Folder> containers = getProjectContainerBrothers(uid, context);
        Folder obj;

        int index = container.getOrder();
        for (int i = 0; i < containers.size(); i++) {
            if (index > containers.get(i).getOrder()) {
                if (elder) {
                    containers.remove(i);
                    i--;
                }
            } else {
                if (!elder) {
                    containers.remove(i);
                    i--;
                }
            }
        }
        return containers;
    }

    /*
     * Get the folder's brothers (same level, same parent)
     */
    public ArrayList<Folder> getProjectContainerBrothers(String uid, XWikiContext context)
        throws XWikiException
    {
        Folder container = getProjectContainer(uid, context);
        ArrayList<Folder> containers;

        if (FOLDERS_ROOT.equals(container.getType())) {
            containers = this.getRootFolders(context);
        } else {
            containers = getProjectContainerChilds(container.getParent(), context);
        }
        Folder obj;
        for (int i = 0; i < containers.size(); i++) {
            obj = containers.get(i);
            if (uid.equals(container.getUid())) {
                containers.remove(i);
            }
        }
        return containers;
    }

    /*
     * Get the folder's childs (same parent)
     */
    public ArrayList<Folder> getProjectContainerChilds(String uid, XWikiContext context)
        throws XWikiException
    {
        ArrayList<Folder> containers = this.getProjectContainers(context);
        projectContainerComparator comp = new projectContainerComparator();

        for (int i = 0; i < containers.size(); i++) {
            if (!uid.equals(containers.get(i).getParent())) {
                containers.remove(i);
                i--;
            }
        }
        Collections.sort(containers, comp);
        return containers;
    }

    /*
     * Get the folder's child at the specified index
     */
    public Folder getProjectContainerChild(String uid, int index, XWikiContext context)
        throws XWikiException
    {
        ArrayList<Folder> childs = getProjectContainerChilds(uid, context);
        Iterator it = childs.iterator();
        Folder obj;

        while (it.hasNext()) {
            obj = (Folder) it.next();
            if (!FOLDERS_ROOT.equals(obj.getType())) { // handle badly filled parent
                if (obj.getOrder() == index) {
                    return obj;
                }
            }
        }
        return null; // TODO : throw exception
    }

    /*
     * Get the root folder at the specified index
     */
    public Folder getRootProjectContainerFromIndex(int index, XWikiContext context)
        throws XWikiException
    {
        ArrayList<Folder> containers = this.getRootFolders(context);
        Iterator it = containers.iterator();
        Folder obj;

        while (it.hasNext()) {
            obj = (Folder) it.next();
            if (obj.getOrder() == index) {
                return obj;
            }
        }
        return null; // TODO : throw exception
    }

    /*
     * Get the roots folders list
     */
    public ArrayList<Folder> getRootFolders(XWikiContext context) throws XWikiException
    {
        ArrayList<Folder> list = null;
        String key = context.getDatabase() + ":" + context.getLocalUser() + ":rootfolders";

        initFoldersCache(context);
        synchronized (key) {
            try {
                // retreive folders list from cache
                list = (ArrayList) foldersCache.getFromCache(key);
            } catch (XWikiCacheNeedsRefreshException e) {
                foldersCache.cancelUpdate(key);

                list = new ArrayList<Folder>();
                List allContainers = this.getProjectContainers(context);
                Iterator it = allContainers.iterator();

                // Build the list from the global folders list
                while (it.hasNext()) {
                    Folder folder = (Folder) it.next();
                    if (FOLDERS_ROOT.equals(folder.getType())) {
                        list.add(folder);
                    }
                }

                // Sort folders list and cache it
                projectContainerComparator comp = new projectContainerComparator();
                Collections.sort(list, comp);
                foldersCache.putInCache(key, list);
            }
        }

        return list;
    }

    /*
     * Get all the folders the user can see
     *
     * @param context the request context
     * @return foldersList
     */
    public ArrayList<Folder> getProjectContainers(XWikiContext context) throws XWikiException
    {
        ArrayList<Folder> list = null;
        String key = context.getDatabase() + ":" + context.getLocalUser() + ":folders";
        initFoldersCache(context);

        synchronized (key) {
            try {
                // retreive folders list from cache
                list = (ArrayList) foldersCache.getFromCache(key);
            } catch (XWikiCacheNeedsRefreshException e) {
                foldersCache.cancelUpdate(key);
                list = new ArrayList<Folder>();

                if (plugin.getUserManager().isAdmin(context) ||
                    plugin.getUserManager().isManager(context))
                {
                    // admin & managers (get all folders)
                    String hql = ", BaseObject as obj where obj.name=doc.fullName"
                        + " and obj.className='" + CLASS_FOLDER + "' and doc.web='" +
                        FOLDERS_SPACE + "'";
                    List containerPages =
                        context.getWiki().getStore().searchDocumentsNames(hql, context);
                    Iterator it = containerPages.iterator();

                    while (it.hasNext()) {
                        String docName = (String) it.next();
                        list.add(
                            new Folder(context.getWiki().getDocument(docName, context), context));
                    }
                } else {
                    // basic users (builds the list from the user's projects)
                    List allProjects = plugin.getProjectManager().getProjects(context);
                    Iterator it = allProjects.iterator();
                    while (it.hasNext()) {
                        Object project = (Object) it.next();
                        String folderName =
                            FOLDERS_SPACE + "." + (String) project.display("container", "view");
                        XWikiDocument currentFolder =
                            context.getWiki().getDocument(folderName, context);
                        if (!currentFolder.isNew()) {
                            // Let's climb the tree to its root (sic)
                            while (currentFolder != null) {
                                // Get the folder object and put it in the list if it's not already in it
                                try {
                                    Folder currentFolderObj = new Folder(currentFolder, context);

                                    // Add the folder to the list
                                    if (!list.contains(currentFolderObj)) {
                                        list.add(currentFolderObj);
                                    }

                                    // Retreive parent
                                    String parent = currentFolderObj.getParent();
                                    if (parent != null && !parent.equals("")) {
                                        currentFolder = context.getWiki()
                                            .getDocument(FOLDERS_SPACE + "." + parent, context);
                                    } else {
                                        currentFolder = null;
                                    }
                                } catch (Exception ex) {
                                    currentFolder = null;
                                }
                            }
                        }
                    }
                }
                foldersCache.putInCache(key, list);
            }
        }

        return (ArrayList<Folder>) list.clone();
    }

    /*
     * Move a folder
     *
     * @param uid folder
     * @param moveUp move up if true, down if false
     * @param context the request context
     * @return replacedUid the uid of the replaced container
     */
    public String moveProjectContainer(String uid, boolean moveUp, XWikiContext context)
        throws XWikiException
    {
        Folder container = getProjectContainer(uid, context);

        int oldindex = container.getOrder();
        int newindex;
        if (moveUp) {
            newindex = oldindex - 1;
        } else {
            newindex = oldindex + 1;
        }
        Folder replacedContainer;
        if (FOLDERS_ROOT.equals(container.getType())) {
            replacedContainer = getRootProjectContainerFromIndex(newindex, context);
        } else {
            replacedContainer = getProjectContainerChild(
                container.getParent(), newindex, context);
        }
        if (replacedContainer != null) {
            replacedContainer.setOrder(container.getOrder(), context);
            container.setOrder(newindex, context);
        } else {
            // TODO : throw exception
        }
        flushFoldersCache();
        return container.getUid() + " " + replacedContainer.getUid();
    }

    /*
     * Get the last index used within the folder
     */
    public int getLastProjectContainerIndex(String parent, XWikiContext context)
        throws XWikiException
    {
        ArrayList<Folder> list;
        if (parent != null && !parent.equals("")) {
            list = getProjectContainerChilds(parent, context);
        } else {
            list = this.getRootFolders(context);
        }
        return list.size() - 1;
    }

    /*
     * Get the HTML color of a folder
     */
    public String getProjectContainerStyle(String uid, XWikiContext context) throws XWikiException
    {
        return getProjectContainer(uid, context).getStyle();
    }

    /*
     * Retreive all the projects within a folder (independantly from folder's level)
     */
    public ArrayList<Object> getProjectContainerProjects(String uid, XWikiContext context)
        throws XWikiException
    {
        ArrayList<Object> list = null;

        String key = context.getDatabase() + ":" + context.getLocalUser() + ":" + uid + ":childs";

        initFoldersCache(context);
        synchronized (key) {
            try {
                // retreive projects list from cache
                list = (ArrayList<Object>) foldersCache.getFromCache(key);
            } catch (XWikiCacheNeedsRefreshException e) {
                // update the folders list cache
                foldersCache.cancelUpdate(key);
                list = new ArrayList<Object>();

                // recursively call this method passing childs UID's
                ArrayList<Folder> childs = getProjectContainerChilds(uid, context);
                Iterator it = childs.iterator();
                while (it.hasNext()) {
                    Folder obj = (Folder) it.next();
                    ArrayList<Object> subList = getProjectContainerProjects(obj.getUid(), context);
                    if (subList != null) {
                        list.addAll(subList);
                    }
                }

                // retreive projects with this container as parent
                List allProjects = plugin.getProjectManager().getProjects(context);
                it = allProjects.iterator();
                while (it.hasNext()) {
                    Object currentProject = (Object) it.next();
                    if (uid.equals(currentProject.display("container", "view"))) {
                        list.add(currentProject);
                    }
                }

                foldersCache.putInCache(key, list);
            }
        }

        return list;
    }

    /*
     * Get the number of projects within the specified folder
     */
    public int getProjectContainerProjectsNumber(String uid, XWikiContext context)
        throws XWikiException
    {
        List projectObjs = getProjectContainerProjects(uid, context);
        if (projectObjs != null) {
            return projectObjs.size();
        }
        return 0;
    }

    /*
     * Compare folders (by indexes)
     */
    public class projectContainerComparator implements Comparator
    {
        public int compare(java.lang.Object o1, java.lang.Object o2)
        {
            return ((Folder)o1).getOrder() - ((Folder)o2).getOrder();
        }
    }
}
