package com.xpn.xwiki.it.selenium.framework;

import junit.framework.Test;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.taskdefs.ExecTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.xpn.xwiki.test.XWikiTestSetup;
import com.xpn.xwiki.test.AntBuildListener;

public class ChronopolysTestSetup extends XWikiTestSetup
{
    private static final String EXECUTION_DIRECTORY = System.getProperty("xwikiExecutionDirectory");
    private static final String START_COMMAND = System.getProperty("xwikiExecutionStartCommand");    
    private static final String PORT = System.getProperty("xwikiPort", "8080");
    private static final boolean DEBUG = System.getProperty("debug", "false").equalsIgnoreCase("true");    
    private static final int TIMEOUT_SECONDS = 60;

    private Project project;

    public ChronopolysTestSetup(Test test)
    {
        super(test);

        this.project = new Project();
        this.project.init();
        this.project.addBuildListener(new AntBuildListener(DEBUG));
    }

    protected void setUp() throws Exception
    {
        System.out.println(EXECUTION_DIRECTORY);
        startXWikiInSeparateThread();
        waitForXWikiToLoad();
    }

    private void startXWikiInSeparateThread()
    {
        Thread startThread = new Thread(new Runnable() {
            public void run() {
                try {
                    startXWiki();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        startThread.start();
    }

    private void startXWiki() throws Exception
    {
        ExecTask execTask = (ExecTask) this.project.createTask("exec");
        execTask.setDir(new File(EXECUTION_DIRECTORY));
        Commandline commandLine = new Commandline(START_COMMAND);
        execTask.setCommand(commandLine);
        execTask.execute();
    }

    private void waitForXWikiToLoad() throws Exception
    {
        // Wait till the main page becomes available which means the server is started fine
        System.out.println("Checking that XWiki is up and running...");
        URL url = new URL("http://localhost:" + PORT + "/xwiki/bin/view/Main/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        boolean connected = false;
        boolean timedOut = false;
        long startTime = System.currentTimeMillis();
        while (!connected && !timedOut) {
            try {
                connection.connect();
                int responseCode = connection.getResponseCode();
                connected = (responseCode == 401);
            } catch (IOException e) {
                // Do nothing as it simply means the server is not ready yet...
            }
            Thread.sleep(100L);
            timedOut = (System.currentTimeMillis() - startTime > TIMEOUT_SECONDS * 1000L);
        }
        if (timedOut) {
            String message = "Failed to start XWiki in [" + TIMEOUT_SECONDS + "] seconds";
            System.out.println(message);
            tearDown();
            throw new RuntimeException(message);
        }
    }
}
