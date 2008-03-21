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
package com.xpn.xwiki.it.selenium.framework;

import junit.framework.TestCase;

import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * All XWiki Selenium tests must extend this class.
 * 
 * @version $Id: $
 */
public class AbstractXWikiTestCase extends TestCase
{
    private SkinExecutor skinExecutor;

    private static final String PORT = System.getProperty("xwikiPort", "8080");

    private static final String BASE_URL = "http://localhost:" + PORT;

    private Selenium selenium;

    public void setSkinExecutor(SkinExecutor skinExecutor)
    {
        this.skinExecutor = skinExecutor;
    }

    public SkinExecutor getSkinExecutor()
    {
        if (this.skinExecutor == null) {
            throw new RuntimeException("Skin executor hasn't been initialized. Make sure to wrap "
                + "your test in a "
                + XWikiTestSuite.class.getName()
                + " class and call "
                + " addTestSuite(Class testClass, SkinExecutor skinExecutor).");
        }
        return this.skinExecutor;
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // Get the browser to test with from a System property set by the Maven2 build.
        // Defaults to Firefox.
        String browser = System.getProperty("browser", "*firefox");

        this.selenium =
            new DefaultSelenium("localhost", SeleniumServer.DEFAULT_PORT, browser, BASE_URL);
        this.selenium.start();
    }

    protected void tearDown() throws Exception
    {
        getSelenium().stop();
    }

    public Selenium getSelenium()
    {
        return this.selenium;
    }

    // Convenience methods wrapping Selenium

    public void open(String url)
    {
        getSelenium().open(url);
    }

    public String getTitle()
    {
        return getSelenium().getTitle();
    }

    public void assertPage(String space, String page)
    {
        assertEquals("XWiki - " + space + " - " + page, getTitle());
    }

    public void assertTitle(String title)
    {
        assertEquals(title, getTitle());
    }

    public boolean isElementPresent(String locator)
    {
        return getSelenium().isElementPresent(locator);
    }

    public boolean isLinkPresent(String text)
    {
        return isElementPresent("link=" + text);
    }

    public void clickLinkWithText(String text)
    {
        clickLinkWithText(text, true);
    }

    public void assertTextPresent(String text)
    {
        assertTrue("[" + text + "] isn't present.", getSelenium().isTextPresent(text));
    }

    public void assertTextNotPresent(String text)
    {
        assertFalse("[" + text + "] is present.", getSelenium().isTextPresent(text));        
    }

    public void assertElementPresent(String elementLocator)
    {
        assertTrue("[" + elementLocator + "] isn't present.", isElementPresent(elementLocator));
    }

    public void assertElementNotPresent(String elementLocator)
    {
        assertFalse("[" + elementLocator + "] is present.", isElementPresent(elementLocator));
    }

    public void waitPage()
    {
        // TODO move magic number to property file?
        waitPage(180000);
    }

    public void waitPage(int nbMillisecond)
    {
        getSelenium().waitForPageToLoad(String.valueOf(nbMillisecond));
    }

    public void clickLinkWithLocator(String locator)
    {
        clickLinkWithLocator(locator, true);
    }

    public void clickLinkWithLocator(String locator, boolean wait)
    {
        assertElementPresent(locator);
        getSelenium().click(locator);
        if (wait) {
            waitPage();
        }
    }

    public void clickLinkWithText(String text, boolean wait)
    {
        clickLinkWithLocator("link=" + text, wait);
    }

    public boolean isChecked(String locator)
    {
        return getSelenium().isChecked(locator);
    }

    public String getFieldValue(String fieldName)
    {
        // Note: We could use getSelenium().getvalue() here. However getValue() is stripping spaces
        // and some of our tests verify that there are leading spaces/empty lines.
        return getSelenium().getEval(
            "selenium.browserbot.getCurrentWindow().document.getElementById(\"" + fieldName
                + "\").value");
    }

    public void setFieldValue(String fieldName, String value)
    {
        getSelenium().type(fieldName, value);
    }

    public void checkField(String locator)
    {
        getSelenium().check(locator);
    }

    public void submit()
    {
        clickLinkWithXPath("//input[@type='submit']");
    }

    public void submit(String locator)
    {
        clickLinkWithLocator(locator);
    }

    public void submit(String locator, boolean wait)
    {
        clickLinkWithLocator(locator, wait);
    }

    public void clickLinkWithXPath(String xpath)
    {
        clickLinkWithXPath(xpath, true);
    }

    public void clickLinkWithXPath(String xpath, boolean wait)
    {
        clickLinkWithLocator("xpath=" + xpath, wait);
    }

    // SkinExecutor methods

    public void clickDeletePage()
    {
        getSkinExecutor().clickDeletePage();
    }

    public void clickEditPreview()
    {
        getSkinExecutor().clickEditPreview();
    }

    public void clickEditSaveAndContinue()
    {
        getSkinExecutor().clickEditSaveAndContinue();
    }

    public void clickEditCancelEdition()
    {
        getSkinExecutor().clickEditCancelEdition();
    }

    public void clickEditSaveAndView()
    {
        getSkinExecutor().clickEditSaveAndView();
    }

    public boolean isAuthenticated()
    {
        return getSkinExecutor().isAuthenticated();
    }

    public void logout()
    {
        getSkinExecutor().logout();
    }

    public void login(String username, String password, boolean rememberme)
    {
        getSkinExecutor().login(username, password, rememberme);
    }

    public void loginAsAdmin()
    {
        getSkinExecutor().loginAsAdmin();
    }

    public void clickLogin()
    {
        getSkinExecutor().clickLogin();
    }

    public void clickRegister()
    {
        getSkinExecutor().clickRegister();
    }

    public void editInWysiwyg(String space, String page)
    {
        getSkinExecutor().editInWysiwyg(space, page);
    }

    public void clearWysiwygContent()
    {
        getSkinExecutor().clearWysiwygContent();
    }

    public void keyPressAndWait(String element, String keycode)
        throws InterruptedException {
        getSelenium().keyPress(element, keycode);       
        waitPage();
    }

    public void typeInWysiwyg(String text)
    {
        getSkinExecutor().typeInWysiwyg(text);
    }

    public void typeInWiki(String text)
    {
        getSkinExecutor().typeInWiki(text);
    }

    public void typeEnterInWysiwyg()
    {
        getSkinExecutor().typeEnterInWysiwyg();
    }

    public void typeShiftEnterInWysiwyg()
    {
        getSkinExecutor().typeShiftEnterInWysiwyg();
    }

    public void clickWysiwygUnorderedListButton()
    {
        getSkinExecutor().clickWysiwygUnorderedListButton();
    }

    public void clickWysiwygOrderedListButton()
    {
        getSkinExecutor().clickWysiwygOrderedListButton();
    }

    public void clickWysiwygIndentButton()
    {
        getSkinExecutor().clickWysiwygIndentButton();
    }

    public void clickWysiwygOutdentButton()
    {
        getSkinExecutor().clickWysiwygOutdentButton();
    }

    public void clickWikiBoldButton()
    {
        getSkinExecutor().clickWikiBoldButton();
    }

    public void clickWikiItalicsButton()
    {
        getSkinExecutor().clickWikiItalicsButton();
    }

    public void clickWikiUnderlineButton()
    {
        getSkinExecutor().clickWikiUnderlineButton();
    }

    public void clickWikiLinkButton()
    {
        getSkinExecutor().clickWikiLinkButton();
    }

    public void clickWikiHRButton()
    {
        getSkinExecutor().clickWikiHRButton();
    }

    public void clickWikiImageButton()
    {
        getSkinExecutor().clickWikiImageButton();
    }

    public void clickWikiSignatureButton()
    {
        getSkinExecutor().clickWikiSignatureButton();
    }

    public void assertWikiTextGeneratedByWysiwyg(String text)
    {
        getSkinExecutor().assertWikiTextGeneratedByWysiwyg(text);
    }

    public void assertHTMLGeneratedByWysiwyg(String xpath) throws Exception
    {
        getSkinExecutor().assertHTMLGeneratedByWysiwyg(xpath);
    }

    public void assertGeneratedHTML(String xpath) throws Exception
    {
        getSkinExecutor().assertGeneratedHTML(xpath);
    }

    public String getUrl(String space, String doc)
    {
        return getUrl(space, doc, "view");
    }

    public String getUrl(String space, String doc, String action)
    {
        return "/xwiki/bin/"+action+"/"+space+"/"+doc;
    }

    public String getUrl(String space, String doc, String action, String param)
    {
        return getUrl(space, doc, action)+"?"+param;
    }
}
