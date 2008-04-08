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

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

public class Folder
{
    private Document docApi;

    private int order = 0;

    private String uid = "";

    private String fullName = "";

    private String parent = "";

    private String style = "";

    private String type = "";

    private String name = "";

    public Folder(String uid, XWikiContext context) throws Exception
    {
        this(context.getWiki().getDocument(FolderManager.FOLDERS_SPACE + "." + uid, context),
            context);
    }

    public Folder(XWikiDocument doc, XWikiContext context) throws ChronopolysPluginException
    {
        if (doc.getObject(FolderManager.CLASS_FOLDER) == null) {
            throw new ChronopolysPluginException(
                ChronopolysPluginException.ERROR_CONTAINER_DOESNOTEXIST,
                "This folder does not exist : " + FolderManager.FOLDERS_SPACE + "." + doc.getName()
            );
        }

        this.docApi = doc.newDocument(context);
        order = Integer.parseInt(doc.display("order", "view", context));
        uid = doc.getName();
        fullName = doc.getFullName();
        parent = doc.display("parent", "view", context);
        type = doc.display("type", "view", context);
        name = doc.display("name", "view", context); 

        try {
            if (FolderManager.FOLDERS_ROOT.equals(type)) {
                style = doc.display("style", "view", context);
            } else {
                Folder parentFolder = new Folder(parent, context);
                style = parentFolder.getStyle();
            }
        } catch (Exception ex) {
            style = "gainsboro";
        }
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order, XWikiContext context) throws XWikiException
    {
        context.getWiki().getDocument(fullName, context).newDocument(context).set("order", order);
    }

    public void decrementOrder(XWikiContext context) throws XWikiException
    {
        setOrder(order - 1, context);
    }

    public void incrementOrder(XWikiContext context) throws XWikiException
    {
        setOrder(order + 1, context);
    }

    public String getUid()
    {
        return uid;
    }

    public String getName()
    {
        return fullName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getParent()
    {
        return parent;
    }

    public String getType()
    {
        return type;
    }

    public String getStyle()
    {
        return style;
    }

    public String display(String prop, String mode)
    {
        if ("edit".equals(mode) || "editnopre".equals(mode)) {
            return docApi.display(prop, mode);
        } else {
            return get(prop);
        }
    }

    // Retro compatibility with api.Object
    public String get(String prop)
    {
        if ("uid".equals(prop)) {
            return getUid();
        } else if ("parent".equals(prop)) {
            return getParent();
        } else if ("style".equals(prop)) {
            return getStyle();
        } else if ("order".equals(prop)) {
            return Integer.toString(getOrder());
        } else if ("type".equals(prop)) {
            return getType();
        } else if ("name".equals(prop)) {
            return this.name;
        }
        return "";
    }
}
