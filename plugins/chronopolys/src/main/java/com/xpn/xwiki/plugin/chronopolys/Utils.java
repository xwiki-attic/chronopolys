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
    
   /**
   * Calculates the number of days between two calendar days 
   * @param d1    The first date.
   * @param d2    The second date.
   * @return      The number of days between the two dates.  Zero is
   *              returned if the dates are the same, one if the dates are
   *              adjacent, etc.  
   *              If Calendar types of d1 and d2
   *              are different, the result may not be accurate.
   */
  public int getDaysBetween (java.util.Calendar d1, java.util.Calendar d2) {
      boolean neg = false;
      if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
          java.util.Calendar swap = d1;
          d1 = d2;
          d2 = swap;
          neg = true;
      }
      int days = d2.get(java.util.Calendar.DAY_OF_YEAR) - d1.get(java.util.Calendar.DAY_OF_YEAR);
      int y2 = d2.get(java.util.Calendar.YEAR);
      if (d1.get(java.util.Calendar.YEAR) != y2) {
          d1 = (java.util.Calendar) d1.clone();
          do {
              days += d1.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
              d1.add(java.util.Calendar.YEAR, 1);
          } while (d1.get(java.util.Calendar.YEAR) != y2);
      }
      if(neg) return (-1) * days;
      return days;
  }
}


