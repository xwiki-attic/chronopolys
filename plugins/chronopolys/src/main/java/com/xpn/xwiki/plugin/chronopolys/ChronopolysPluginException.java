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

import com.xpn.xwiki.plugin.PluginException;

public class ChronopolysPluginException extends PluginException
{
    public static final int ERROR_PROJECT_ALREADYEXIST = 80001;

    public static final int ERROR_PROJECT_DOESNOTEXIST = 80002;

    public static final int ERROR_CONTAINER_DOESNOTEXIST = 80003;

    public static final int ERROR_CONTAINER_ISNOTVALID = 80004;

    public static final int ERROR_PHASE_DOESNOTEXIST = 80005;

    public ChronopolysPluginException(int code, String message)
    {
        super(com.xpn.xwiki.plugin.chronopolys.ChronopolysPlugin.class, code, message);
    }

    public ChronopolysPluginException(int code, String message, Throwable e, Object[] args)
    {
        super(com.xpn.xwiki.plugin.chronopolys.ChronopolysPlugin.class, code, message, e, args);
    }

    public ChronopolysPluginException(int code, String message, Throwable e)
    {
        super(com.xpn.xwiki.plugin.chronopolys.ChronopolysPlugin.class, code, message, e);
    }
}