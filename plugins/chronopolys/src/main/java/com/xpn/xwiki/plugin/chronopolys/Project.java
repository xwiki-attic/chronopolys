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

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.cache.api.XWikiCache;
import com.xpn.xwiki.cache.api.XWikiCacheNeedsRefreshException;
import com.xpn.xwiki.cache.api.XWikiCacheService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Project
{
    public static final String CLASS_PROJECT =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "ProjectClass";

    public static final String CLASS_NOTE =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "ProjectNote";

    public static final String PROJECT_SPACEPREFIX = "Project_";

    public static final String PROJECT_HOMEDOC = "WebHome";

    public static final String PROJECT_LOGDOC = "ProjectLog";

    public static final String PROJECT_DOCUMENTSDOC = "ProjectDocuments";

    public static final String PROJECT_MEMBERSDOC = "ProjectMembers";

    public static final String PROJECT_GUESTSDOC = "ProjectGuests";

    public static final String PROJECT_LEADERSDOC = "ProjectLeaders";

    public static final String PROJECT_PHASESDOC = "ProjectPhases";

    public static final String PROJECT_WIKIDOC = "ProjectWiki";

    public static final String PROJECT_NOTEDOC = "ProjectNote";

    public static final String PROJECT_PREFSDOC = "WebPreferences";

    public static final String PROJECT_DEFAULT_CONTAINER = "OrphanActivity";

    // List of all the project system documents
    public static final String CO = ",";

    public static final String DEFAULT_PROJECT_DOCS =
        PROJECT_HOMEDOC + CO + PROJECT_PREFSDOC + CO + PROJECT_LOGDOC
            + CO + PROJECT_LEADERSDOC + CO + PROJECT_MEMBERSDOC
            + CO + PROJECT_PHASESDOC + CO + PROJECT_DOCUMENTSDOC
            + CO + PROJECT_WIKIDOC + CO + PROJECT_GUESTSDOC + CO + PROJECT_NOTEDOC;

    private ChronopolysPlugin plugin;

    private Document doc;

    private Object objChronoProject;

    private ProjectLog log;

    private ProjectWiki wiki;

    private ProjectMembers members;

    private ProjectGuests guests;

    private ProjectPhases phases;

    private ProjectNotifications notifications;

    public Project(XWikiDocument doc, ChronopolysPlugin plugin, XWikiContext context)
        throws XWikiException
    {
        this.plugin = plugin;

        if (doc == null) {
            throw new ChronopolysPluginException(
                ChronopolysPluginException.ERROR_PROJECT_DOESNOTEXIST, "This project does not exist"
            );
        }

        this.doc = new Document(doc, context);

        if (this.doc.getObjectNumbers(CLASS_PROJECT) == 0) {
            throw new ChronopolysPluginException(
                ChronopolysPluginException.ERROR_PROJECT_DOESNOTEXIST, "This project does not exist"
            );
        } else {
            this.objChronoProject = this.doc.getObject(CLASS_PROJECT);
        }

        // init child instances
        this.log = new ProjectLog(this);
        this.wiki = new ProjectWiki(this);
        this.members = new ProjectMembers(this);
        this.guests = new ProjectGuests(this);
        this.phases = new ProjectPhases(this);
        this.notifications = new ProjectNotifications(this);
    }

    public ChronopolysPlugin getPlugin()
    {
        return this.plugin;
    }

    /* *********************************************************************************************
     * Subclasses
     */

    public ProjectLog getLog()
    {
        return this.log;
    }

    public ProjectWiki getWiki()
    {
        return this.wiki;
    }

    public ProjectMembers getMembers()
    {
        return this.members;
    }

    public ProjectGuests getGuests()
    {
        return this.guests;
    }

    public ProjectPhases getPhases()
    {
        return this.phases;
    }

    public ProjectNotifications getNotifications()
    {
        return this.notifications;
    }

    /* *********************************************************************************************
     * Project last modifications
     */

    public List getLastModifications(int limit, int start, XWikiContext context)
        throws XWikiException
    {
        return plugin.getUtils().intelliSubList(limit, start, this.getLastModifications(context));
    }

    public List getLastModifications(XWikiContext context) throws XWikiException
    {
        String key = this.doc.getSpace() + "-lastmodifications";
        ArrayList<Modification> list = null;
        this.plugin.getProjectManager().initProjectsCache(context);
        XWikiCache projectCache = this.plugin.getProjectManager().getProjectsCache();

        synchronized (key) {
            try {
                list = (ArrayList<Modification>) projectCache.getFromCache(key);
            } catch (XWikiCacheNeedsRefreshException e) {
                projectCache.cancelUpdate(key);
                list = new ArrayList<Modification>();
                String hql = " where doc.web='" + doc.getSpace() +
                    "' and doc.name != 'WebPreferences'" + "order by doc.date desc";
                List docList = context.getWiki().getStore().searchDocumentsNames(hql, context);

                for (Iterator i = docList.iterator(); i.hasNext();) {
                    String modName = (String) i.next();
                    XWikiDocument modDoc = context.getWiki().getDocument(modName, context);
                    Date date = modDoc.getDate();
                    list.add(new Modification(modDoc.getFullName(), modDoc.getDisplayTitle(context),
                        modDoc.getComment(), date, this.doc.display("name", "view")));
                }

                projectCache.putInCache(key, list);
            }
        }
        
        return list;
    }

    /* *********************************************************************************************
    * Project home
    */

    public String getLanguage()
    {
        return "fr";
    }

    public String getSpace()
    {
        return doc.getSpace();
    }

    public String getName()
    {
        return doc.getName();
    }

    public String getFullName()
    {
        return doc.getFullName();
    }

    public String getURL(XWikiContext context) throws XWikiException
    {
        return context.getWiki().getExternalURL(doc.getFullName(), "view", context);
    }

    public XWikiDocument getProjectHomeDoc(XWikiContext context) throws XWikiException
    {
        return context.getWiki().getDocument(doc.getFullName(), context);
    }

    /* *********************************************************************************************
     * Project space
     */

    public void delete(XWikiContext context) throws XWikiException
    {
        String hql =
            "select doc.fullName from XWikiDocument doc where doc.web='" + doc.getSpace() + "'";
        List allPages = context.getWiki().search(hql, context);
        Iterator it = allPages.iterator();
        XWiki wiki = context.getWiki();

        while (it.hasNext()) {
            String docName = (String) it.next();
            wiki.deleteDocument(wiki.getDocument(docName, context), context);
        }

        plugin.getProjectManager().flushProjectsCache();
        plugin.getFolderManager().flushFoldersCache();
        plugin.getUserManager().flushUserdataCache();
    }

    public List getProjectPages()
    {
        return Arrays.asList(DEFAULT_PROJECT_DOCS.split(","));
    }

    /* *********************************************************************************************
    * XWikiObject (CLASS_PROJECT)
    */

    public Object getXWikiObject()
    {
        return objChronoProject;
    }

    public java.lang.Object get(String name)
    {
        return doc.getValue(name);
    }

    public void set(String name, String value) throws XWikiException
    {
        getXWikiObject().set(name, value);
        doc.saveWithProgrammingRights();
    }

    /* *********************************************************************************************
    * Project notes
    */

    public List getNoters(XWikiContext context) throws XWikiException
    {
        ArrayList<String> list = new ArrayList<String>();
        List notes = this.getRawNotesAsList(context);
        if (notes != null) {
            for (Iterator it = notes.iterator(); it.hasNext();) {
                String entry = (String) it.next();
                if (entry.matches(".*=[0-5]")) {
                    list.add((entry.split("=")[0]));
                }
            }
        }
        return list;
    }

    public List getNotes(XWikiContext context) throws XWikiException
    {
        ArrayList<String> list = new ArrayList<String>();
        List notes = this.getRawNotesAsList(context);
        if (notes != null) {
            for (Iterator it = notes.iterator(); it.hasNext();) {
                String entry = (String) it.next();
                if (entry.matches(".*=[0-5]")) {
                    list.add((entry.split("=")[1]));
                }
            }
        }
        return list;
    }

    public String getRawNotes(XWikiContext context) throws XWikiException
    {
        XWikiDocument noteDoc = context.getWiki().getDocument(doc.getSpace() + "." + PROJECT_NOTEDOC, context);
        if (!noteDoc.isNew()) {
            String stringNotes = (String) noteDoc.display("notes", context);
            return stringNotes;
        } else {
            return "";
        }
    }

    public List getRawNotesAsList(XWikiContext context) throws XWikiException
    {
        List notes;
        String stringNotes = this.getRawNotes(context);
        notes = Arrays.asList(stringNotes.split("\\|"));
        return notes;
    }

    public void resetNotes(XWikiContext context) throws XWikiException
    {
        XWikiDocument noteDoc = context.getWiki().getDocument(PROJECT_NOTEDOC, context);
        noteDoc.setStringValue(CLASS_NOTE, "notes", "");
        noteDoc.setComment("resetednotes");
        context.getWiki().saveDocument(noteDoc, context);
    }

    public float getNote(XWikiContext context) throws XWikiException
    {
        float total = 0;
        Collection notes = this.getNotes(context);
        for (Iterator it = notes.iterator(); it.hasNext();) {
            int current = Integer.parseInt((String) it.next());
            total += current;
        }

        if (!Float.isNaN(total / notes.size())) {
            return total / notes.size();
        } else {
            return 0;
        }
    }

    public void addNote(String note, XWikiContext context) throws XWikiException
    {
        String user = context.getLocalUser();
        String currentNotes = this.getRawNotes(context);
        XWikiDocument pr =
            context.getWiki().getDocument(doc.getSpace() + "." + PROJECT_NOTEDOC, context);
        if (!hasGivenNote(context)) {
            pr.getObject(CLASS_NOTE).set("notes", currentNotes + user + "=" + note + "|", context);
            pr.setComment("addednote|" + context.getLocalUser());
        } else {
            pr.getObject(CLASS_NOTE).set("notes", currentNotes.replaceFirst(user + "=[0-5]", user + "=" + note),
                context);
            pr.setComment("modifiednote|" + context.getLocalUser());
        }
        context.getWiki().saveDocument(pr, context);
    }

    public boolean hasGivenNote(XWikiContext context) throws XWikiException
    {
        return this.getNoters(context).contains(context.getLocalUser());
    }
}