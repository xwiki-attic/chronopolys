/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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
 */
package com.xpn.xwiki.it.selenium;

import com.xpn.xwiki.it.selenium.framework.AbstractXWikiTestCase;
import com.xpn.xwiki.it.selenium.framework.ChronopolysSkinExecutor;
import com.xpn.xwiki.it.selenium.framework.XWikiTestSuite;

import junit.framework.Test;

/**
 * Tries to register new chronopolys users
 *
 * @version $Id: $
 */
public class RegisterTest extends AbstractXWikiTestCase {
    public static Test suite()
    {
        XWikiTestSuite suite = new XWikiTestSuite("Tries to register new chronopolys users");
        suite.addTestSuite(RegisterTest.class, ChronopolysSkinExecutor.class);
        return suite;
    }

    public void setUp() throws Exception
    {
        super.setUp();
        login("Administrator", "admin", true);
    }

    private void fillFormWithDefaultValues(String xwikiname) {
        setFieldValue("register_first_name", "John");
        setFieldValue("register_last_name", "Smith");
        setFieldValue("xwikiname", xwikiname);
        setFieldValue("register_password", "admin");
        setFieldValue("register2_password", "admin");
        setFieldValue("register_email", "john@example.com");
    }

    private void registerUser(String xwikiname)
    {
        open(getUrl("Main", "Users"));
        getSelenium().click("//form/div[1]/span");
        fillFormWithDefaultValues(xwikiname);
        submit();
        assertTextPresent("Registration successful");
        clickLinkWithLocator("link=John Smith");
        assertTextPresent("Profile of John Smith");
        login(xwikiname, "admin", false);
    }

    public void testRegisterUsers()
    {
        registerUser("Manager");
        registerUser("User");
    }

    public void testRegisterExistingUser()
    {
        open(getUrl("Main", "Users"));
        getSelenium().click("//form/div[1]/span");
        fillFormWithDefaultValues("Admin");

        submit();

        assertTextPresent("User already exists.");
    }
}
