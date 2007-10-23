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
import com.xpn.xwiki.doc.XWikiDocument;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ProjectLog
{
    public static final String CLASS_PROJECTARTICLE =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "ProjectArticleClass";

    public static final String CLASS_PROJECTARTICLERSVP =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "ProjectArticleRsvpClass";

    public static final String PROJECT_PLOGPREFIX = "Plog_";

    public static final String PLOGTASK = "task";

    public static final String PLOGMEETING = "meeting";

    Project project;

    public ProjectLog(Project project)
    {
        this.project = project;
    }

    public String getNewPlogUid(XWikiContext context) throws XWikiException
    {
        String uid;
        String page;

        uid = PROJECT_PLOGPREFIX + org.apache.commons.lang.RandomStringUtils.randomNumeric(8);
        page = project.getSpace() + ChronopolysPlugin.SEP + uid;
        while (context.getWiki().exists(page, context)) {
            uid = PROJECT_PLOGPREFIX + org.apache.commons.lang.RandomStringUtils.randomNumeric(8);
            page = project.getSpace() + ChronopolysPlugin.SEP + uid;
        }

        return uid;
    }

    /*
    ** TODO: cache blog entries ?
    */
    public List getPlogPages(XWikiContext context) throws XWikiException
    {
        ArrayList<String> pages = new ArrayList<String>();
        String hql =
            "select doc.name from XWikiDocument doc where doc.web='" + project.getSpace() + "'";
        List allPages = context.getWiki().search(hql, context);
        List hiddenPages = project.getProjectPages();
        Iterator it = allPages.iterator();

        while (it.hasNext()) {
            String docName = (String) it.next();
            if (!hiddenPages.contains(docName)) {
                if (context.getWiki().getDocument(project.getSpace()
                    + ChronopolysPlugin.SEP
                    + docName, context).getObjectNumbers(CLASS_PROJECTARTICLE) > 0)
                {
                    pages.add(docName);
                }
            }
        }
        return pages;
    }

    public boolean isPlogPage(String page, XWikiContext context) throws XWikiException
    {
        return this.getPlogPages(context).contains(page);
    }

    public boolean isPlogTask(XWikiDocument plogdoc)
    {
        return plogdoc.getStringValue("type").equals(PLOGTASK);
    }

    public boolean isPlogMeeting(XWikiDocument plogdoc)
    {
        return plogdoc.getStringValue("type").equals(PLOGMEETING);
    }

    public boolean isPlogNew(XWikiDocument plogdoc)
    {
        return plogdoc.getVersion().equals("1.1");
    }
}
