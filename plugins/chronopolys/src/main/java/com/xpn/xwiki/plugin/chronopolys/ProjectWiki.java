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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ProjectWiki
{
    Project project;

    public ProjectWiki(Project project)
    {
        this.project = project;
    }

    /*
    ** TODO: cache wiki pages ?
    */
    public List getWikiPages(XWikiContext context) throws XWikiException
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
                XWikiDocument tdoc =
                    context.getWiki().getDocument(project.getSpace() + "." + docName, context);
                if (tdoc.getObjectNumbers(ProjectLog.CLASS_PROJECTARTICLE) == 0
                    && tdoc.getObjectNumbers(ProjectLog.CLASS_PROJECTARTICLERSVP) == 0)
                {
                    pages.add(docName);
                }
            }
        }
        return pages;
    }

    public boolean isWikiPage(String page, XWikiContext context) throws XWikiException
    {
        return this.getWikiPages(context).contains(page);
    }
}
