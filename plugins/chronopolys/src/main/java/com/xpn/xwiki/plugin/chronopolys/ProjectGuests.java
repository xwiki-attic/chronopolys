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

public class ProjectGuests
{
    public static final String CLASS_MEMBERSHIP = "XWiki.XWikiGroups";

    public static final String XWIKIALLGROUP = "XWiki.XWikiAllGroup";

    Project project;

    public ProjectGuests(Project project)
    {
        this.project = project;
    }

    public Document getGuestDoc(XWikiContext context) throws XWikiException {
        return context.getWiki()
            .getDocument(project.getSpace(), Project.PROJECT_GUESTSDOC, context)
            .newDocument(context);
    }

    public boolean addGuest(String name, XWikiContext context) throws XWikiException
    {
        if (this.isGuest(name, context)) {
            return false;
        }

        Document projectGuests = this.getGuestDoc(context);
        int nb = projectGuests.createNewObject("XWiki.XWikiGroups");
        com.xpn.xwiki.api.Object obj = projectGuests.getObject("XWiki.XWikiGroups", nb);
        obj.set("member", name);
        projectGuests.save("addguest|" + context.getWiki().getLocalUserName(name, context));

        // invalidate ChronopolysPlugin caches
        project.getPlugin().flushCache();

        return true;
    }

    public boolean isGuest(String docName, XWikiContext context) throws XWikiException
    {
        List users = this.getGuests(context);
        return users.contains(docName);
    }

    public List getGuests(XWikiContext context) throws XWikiException
    {
        Document projectGuests = this.getGuestDoc(context);
        List objs = projectGuests.getObjects("XWiki.XWikiGroups");
        Iterator it = objs.iterator();
        List guests = new ArrayList();
        while (it.hasNext()) {
            guests.add(((Object) it.next()).display("member", "view"));
        }

        return guests;
    }

    public boolean removeGuest(String guest, XWikiContext context) throws XWikiException
    {
        if (!this.isGuest(guest, context)) {
            return false;
        }

        Document projectGuests = this.getGuestDoc(context);

        // Revoke membership
        Vector objs = projectGuests.getObjects(CLASS_MEMBERSHIP);
        Object obj;
        Iterator it = objs.iterator();
        while (it.hasNext()) {
            obj = (Object) it.next();
            if (obj.get("member").equals(guest)) {
                projectGuests.removeObject(obj);
            }
        }        
        projectGuests.save("removemember|" + context.getWiki().getLocalUserName(guest, context));

        // invalidate ChronopolysPlugin caches
        project.getPlugin().flushCache();

        return true;
    }

    public boolean isPublic(XWikiContext context) throws XWikiException {
        return isGuest(XWIKIALLGROUP, context);
    }

    public boolean setPublic(boolean makepublic, XWikiContext context) throws XWikiException {
        if (makepublic) {
            if (this.isPublic(context)) {
                return false;
            } else {
                this.addGuest(XWIKIALLGROUP, context);
            }
        } else {
            if (!this.isPublic(context)) {
                return false;
            } else {
                this.removeGuest(XWIKIALLGROUP, context);
            }
        }

        // invalidate ChronopolysPlugin caches
        project.getPlugin().flushCache();

        return true;
    }
}
