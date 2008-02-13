package com.xpn.xwiki.plugin.chronopolys;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.SMTPServer;

public class NanoSMTP implements MessageListener
{
	SMTPServer server;

	List<NanoSMTPMessage> messages = Collections.synchronizedList(new ArrayList<NanoSMTPMessage>());

	/**
	 * Create a new SMTP server with this class as the listener.
	 * The default port is set to 25. Call setPort()/setHostname() before
	 * calling start().
	 */
	public NanoSMTP()
	{
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);

		this.server = new SMTPServer(listeners);
		this.server.setPort(25);
        this.server.setMaxConnections(30000);
	}

	/**
	 * The port that the server should listen on.
	 * @param port
	 */
	public void setPort(int port)
	{
		this.server.setPort(port);
	}

	/**
	 * The hostname that the server should listen on.
	 * @param hostname
	 */
	public void setHostname(String hostname)
	{
		this.server.setHostName(hostname);
	}

	/**
	 * Starts the SMTP Server
	 */
	public void start()
	{
		this.server.start();
	}

	/**
	 * Stops the SMTP Server
	 */
	public void stop()
	{
		this.server.stop();
	}

	/**
	 * Always accept everything
	 */
	public boolean accept(String from, String recipient)
	{
		return true;
	}

	/**
	 * Cache the messages in memory. Now avoids unnecessary memory copying.
	 */
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
	{
		NanoSMTPMessage msg = new NanoSMTPMessage(this, from, recipient, data);
		messages.add(msg);
	}

	/**
	 * Creates the JavaMail Session object for use in WiserMessage
	 */
	protected Session getSession()
	{
		return Session.getDefaultInstance(new Properties());
	}

	/**
	 * @return the list of WiserMessages
	 */
	public List<NanoSMTPMessage> getMessages()
	{
		return this.messages;
	}

	public SMTPServer getServer()
	{
		return this.server;
	}

	/**
	 * Creates the AuthHandlerFactory which logs the user/pass.
	 */
	/* public class AuthHandlerFactory implements AuthenticationHandlerFactory
	{
		public AuthenticationHandler create()
		{
			PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
			UsernamePasswordValidator validator = new UsernamePasswordValidator()
			{
				public void login(String username, String password)
						throws LoginFailedException
				{
					log.debug("Username=" + username);
					log.debug("Password=" + password);
				}
			};
			ret.addPlugin(new PlainAuthenticationHandler(validator));
			ret.addPlugin(new LoginAuthenticationHandler(validator));
			return ret;
		}
	} */
}
