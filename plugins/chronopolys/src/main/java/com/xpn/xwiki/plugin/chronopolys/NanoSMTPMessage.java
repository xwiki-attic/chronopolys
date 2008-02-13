package com.xpn.xwiki.plugin.chronopolys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class NanoSMTPMessage
{
    private static final Log LOG = LogFactory.getLog(NotificationManager.class);

    NanoSMTP smtp;
	String envelopeSender;
	String envelopeReceiver;
	InputStream stream;
	byte[] array;
	MimeMessage message = null;

	NanoSMTPMessage(NanoSMTP smtp, String envelopeSender, String envelopeReceiver, InputStream stream)
	{
		this.smtp = smtp;
		this.envelopeSender = envelopeSender;
		this.envelopeReceiver = envelopeReceiver;
		this.stream = stream;
	}

	/**
	 * Generate a JavaMail MimeMessage.
	 * @throws MessagingException
	 */
	public MimeMessage getMimeMessage() throws MessagingException
	{
		if (message == null)
		{
			 message = new MimeMessage(this.smtp.getSession(), stream);
		}
		return message;
	}

	/**
	 * Get's the raw message DATA.
	 * Note : this could result in loading many data into memory in case of big
	 * attached files. This is why the array is only generated on the first call.
	 *
	 * @return the byte array of the raw message or an empty byte array
	 * if an exception occured.
	 */
	public byte[] getData()
	{
		if (array == null)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BufferedInputStream in;

			if (stream instanceof BufferedInputStream)
				in = (BufferedInputStream) stream;
			else
				in = new BufferedInputStream(this.stream);

			// read the data from the stream
			try
			{
				int b;
				byte[] buf = new byte[8192];
				while ((b = in.read(buf)) >= 0)
				{
					out.write(buf, 0, b);
				}

				array = out.toByteArray();
			}
			catch (IOException ioex)
			{
				array = new byte[0];
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (IOException e) {}
			}
		}

		return array;
	}

	/**
	 * Get's the RCPT TO:
	 */
	public String getEnvelopeReceiver()
	{
		return this.envelopeReceiver;
	}

	/**
	 * Get's the MAIL FROM:
	 */
	public String getEnvelopeSender()
	{
		return this.envelopeSender;
	}

	public void dispose()
	{
		try
		{
			finalize();
		}
		catch (Throwable t)
		{
		}
	}

	protected void finalize() throws Throwable
	{
		super.finalize();
		if (this.stream != null)
			this.stream.close();
	}
}

