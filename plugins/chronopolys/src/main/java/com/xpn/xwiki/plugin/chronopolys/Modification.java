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

import java.util.Date;
import java.text.SimpleDateFormat;

public class Modification implements Comparable
{
        public String page;

        public String name;

        public String comment;

        public Date date;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd, HH:mm");

        /*
         * Constructor
         */
        public Modification(String page, String name, String comment, Date date, String projectName)
        {
            this.page = page;
            this.name = name;
            this.comment = comment;
            this.date = date;
        }       

        /*
         * Compare modifications (desc order)
         */
        public int compareTo(java.lang.Object modification)
        {
            Modification d2 = (Modification) modification;
            return d2.date.compareTo(this.date);
        }

        /*
         * Return modification page
         */
        public String getPage()
        {
            return this.page;
        }

        /*
         * Return modification name
         */
        public String getName()
        {
            return this.name;
        }


        /*
         * Return modification comment
         */
        public String getComment()
        {
            return this.comment;
        }

        /*
         * Return modification date
         */
        public Date getDate()
        {
            return this.date;
        }

}
