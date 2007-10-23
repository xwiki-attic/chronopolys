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

public class Deadline implements Comparable
{
    public String page;

    public String name;

    public int type;

    public Date date;

    /*
     * Constructor
     */
    public Deadline(String page, String name, int type, Date date)
    {
        this.page = page;
        this.name = name;
        this.type = type;
        this.date = date;
    }

    /*
     * Compare deadlines (by date)
     */
    public int compareTo(java.lang.Object deadline)
    {
        Deadline d2 = (Deadline) deadline;
        return this.date.compareTo(d2.date);
    }

    /*
     * Return deadline page
     */
    public String getPage()
    {
        return this.page;
    }

    /*
     * Return deadline name
     */
    public String getName()
    {
        return this.name;
    }

    /*
     * Return deadline type
     */
    public int getType()
    {
        return this.type;
    }

    /*
     * Return deadline date
     */
    public Date getDate()
    {
        return this.date;
    }
}
