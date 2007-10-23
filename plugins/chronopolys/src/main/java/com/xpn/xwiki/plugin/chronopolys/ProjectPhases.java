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
import com.xpn.xwiki.doc.XWikiDocument;

import java.util.*;

public class ProjectPhases
{
    public static final String CLASS_PHASE =
        ChronopolysPlugin.CLASS_DEFAULT_SPACE + "." + "ProjectPhaseClass";

    Project project;

    public ProjectPhases(Project project)
    {
        this.project = project;
    }

    /* public boolean addPhase(String name, XWikiContext context) throws XWikiException {
       if (isPhase(name, context))
           return false;
       XWikiDocument projectPhasesXWiki = context.getWiki().getDocument(doc.getSpace(), PROJECT_PHASESDOC, context);
       Document projectPhases = new Document(projectPhasesXWiki, context);
       int nb = projectPhases.createNewObject(CLASS_PHASE);
       Object obj = projectPhases.getObject(CLASS_PHASE, nb);
       obj.set("name", name);
       save(context);
       return true;
   } */

    /* public void updatePhase(String name, XWikiContext context) throws XWikiException {
        if (!isPhase(name, context))
            throw new PluginException(CLASS_PROJECT, ERROR_PHASE_DOESNOTEXIST, "This phase does not exist");
        doc.updateObjectFromRequest(CLASS_PHASE);
        doc.save();
    } */

    /* public boolean removePhase(String name, XWikiContext context) throws XWikiException {
        if (!isPhase(name, context))
            return false;
        XWikiDocument projectPhasesXWiki = context.getWiki().getDocument(doc.getSpace(), PROJECT_PHASESDOC, context);
        Document projectPhases = new Document(projectPhasesXWiki, context);
        Vector objs = projectPhases.getObjects(CLASS_PHASE);
        Object obj;
        Iterator it = objs.iterator();

        while (it.hasNext()) {
            obj = (Object) it.next();
            if (obj.get("name").equals(name)) {
                doc.removeObject(obj);
                save(context);
                return true;
            }
        }
        return false;
    } */

    public boolean isPhase(String name, XWikiContext context) throws XWikiException
    {
        List phases = getPhasesNames(context);
        return phases.contains(name);
    }

    public List getPhasesNames(XWikiContext context) throws XWikiException
    {
        XWikiDocument projectPhasesXWiki =
            context.getWiki().getDocument(project.getSpace(), Project.PROJECT_PHASESDOC, context);
        Document projectPhases = new Document(projectPhasesXWiki, context);
        List objs = projectPhases.getObjects(CLASS_PHASE);
        Iterator it = objs.iterator();
        List phases = new ArrayList();
        while (it.hasNext()) {
            phases.add(((com.xpn.xwiki.api.Object) it.next()).get("name"));
        }
        return phases;
    }

    public List getPhases(XWikiContext context) throws XWikiException
    {
        StringBuffer output = new StringBuffer();
        XWikiDocument projectPhasesXWiki =
            context.getWiki().getDocument(project.getSpace(), Project.PROJECT_PHASESDOC, context);
        Document projectPhases = new Document(projectPhasesXWiki, context);
        List objs = projectPhases.getObjects(CLASS_PHASE);
        phaseComparator comp = new phaseComparator();

        Collections.sort(objs, comp);
        return objs;
    }

    public class phaseComparator implements Comparator
    {
        public int compare(java.lang.Object o1, java.lang.Object o2)
        {
            if (!((Object) o1).get("start").toString().equals("") &&
                !((Object) o2).get("start").toString().equals(""))
            {
                String[] startS = ((Object) o1).get("start").toString().split("/");
                String[] endS = ((Object) o2).get("start").toString().split("/");
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.set(Integer.valueOf(startS[2]), Integer.valueOf(startS[1]),
                    Integer.valueOf(startS[0]));
                cal2.set(Integer.valueOf(endS[2]), Integer.valueOf(endS[1]),
                    Integer.valueOf(endS[0]));

                return cal1.compareTo(cal2);
            }
            return 0;
        }
    }
}
