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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.SortTool;

public class Utils
{
    public String arrayToString(String[] a, String separator)
    {
        String result = "";
        if (a != null) {
            if (a.length > 0) {
                result = a[0];    // start with the first element
                for (int i = 1; i < a.length; i++) {
                    result = result + separator + a[i];
                }
            }
        }
        return result;
    }

    public String collectionToString(Collection col, String separator)
    {
        String result = "";
        String sep = "";
        if (col != null) {
            int size = col.size();
            Iterator it = col.iterator();
            while (it.hasNext()) {
                String item = (String) it.next();
                result = result + sep + item;
                sep = separator;
            }
        }
        return result;
    }

    public String getRandomAlphanumeric(int length)
    {
        return org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(length);
    }

    public List getNewList()
    {
        return new ArrayList();
    }

    public ListTool getListTool()
    {
        return new ListTool();
    }

    public SortTool getSortTool()
    {
        return new SortTool();
    }

    public boolean isDate(String date)
    {
        SimpleDateFormat df = new SimpleDateFormat();
        try {
            df.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /*
    * Get files located in /xwiki/static (used for HTML emails)
    */
    public String getStaticSkinFile(String filename, XWikiContext context) throws XWikiException
    {
        return "http://" + context.getWiki().getXWikiPreference("server_name", context) +
            "/xwiki/static/" + filename;
    }

    /*
     * Behave like List.subList with a precheck on the given indexes
     *
     * @param limit end index
     * @param start start index
     * @param list the original list
     * @return sublist the sublist
     */
    public List intelliSubList(int limit, int start, List list)
    {
        int min = start;
        if (start < 0 || start > list.size()) {
            min = 0;
        }
        int max = start + limit;
        if (max > list.size()) {
            max = list.size();
        }
        return list.subList(min, max);
    }
}
