package com.fw.webutil.service.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.fw.ccg.xml.XMLBeanParser;

/**
 * Service to send mails
 * 
 * @author akiran
 */
public class EmailService
{
	private EmailServiceConfiguration configuration;
	private Properties configProperties;

	private Map<String, EmailTemplate> templateMap = new HashMap<>();

	public void setConfiguration(EmailServiceConfiguration configuration)
	{
		this.configuration = configuration;
	}

	@PostConstruct
	private void init()
	{
		// make sure configuration is provided and it is valid
		if(configuration == null)
		{
			throw new IllegalStateException("No configuration is provided");
		}

		configuration.validate();

		// get java mail properties from configuration
		configProperties = configuration.toProperties();

		// load email templates, if any
		EmailTemplateFile templateFile = new EmailTemplateFile();

		for(String resource : configuration.getTemplateResources())
		{
			XMLBeanParser.parse(EmailService.class.getResourceAsStream(resource), templateFile);
		}

		this.templateMap = templateFile.getTemplates();
	}

	/**
	 * Create new java mail session with the configuration provided to the
	 * service
	 *
	 * @return
	 */
	private Session newSession()
	{
		Session mailSession = null;

		//if authentication needs to be done provide user name and password
		if(configuration.isUseAuthentication())
		{
			mailSession = Session.getInstance(configProperties, new Authenticator()
			{
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(configuration.getUserName(), configuration.getPassword());
				}
			});
		}
		else
		{
			mailSession = Session.getInstance(configProperties);
		}

		return mailSession;
	}

	/**
	* Converts provided list of email string to InternetAddress objects
	*
	* @param ids
	* @return
	* @throws AddressException
	*/
	private InternetAddress[] convertToInternetAddress(String ids[]) throws AddressException
	{
		InternetAddress[] res = new InternetAddress[ids.length];

		for(int i = 0; i < ids.length; i++)
		{
			res[i] = new InternetAddress(ids[i]);
		}

		return res;
	}

	/**
	* Checks if provided string array is null or empty
	*
	* @param str
	* @return
	*/
	private boolean isEmpty(String str[])
	{
		return(str == null || str.length == 0);
	}

	/**
	* Builds the fully composed mail message that can be sent
	*
	* @param recipientsToList
	* @param recipientsCcList
	* @param recipientsBccList
	* @param fromMailId
	* @param subject
	* @param messageBody
	* @return
	* @throws AddressException
	* @throws MessagingException
	*/
	private Message buildMessage(String recipientsToList[], String recipientsCcList[], String recipientsBccList[], String fromMailId,
			String subject, String messageBody) throws AddressException, MessagingException
	{
		if(isEmpty(recipientsToList) && isEmpty(recipientsCcList) && isEmpty(recipientsBccList))
		{
			throw new IllegalArgumentException("No recipient is specified in any list");
		}

		// Create mail message
		Session mailSession = newSession();

		// build the mail message
		Message message = new MimeMessage(mailSession);

		message.setFrom(new InternetAddress(fromMailId));

		//set recipients mail lists
		if(!isEmpty(recipientsToList))
		{
			message.setRecipients(Message.RecipientType.TO, convertToInternetAddress(recipientsToList));
		}

		if(!isEmpty(recipientsCcList))
		{
			message.setRecipients(Message.RecipientType.CC, convertToInternetAddress(recipientsCcList));
		}

		if(!isEmpty(recipientsBccList))
		{
			message.setRecipients(Message.RecipientType.BCC, convertToInternetAddress(recipientsBccList));
		}

		//set the subject and body
		message.setSubject(subject);
		message.setText(messageBody);

		return message;
	}

	/**
	* Service method to send mails. This method uses the specified template to compute the subject and the body
	*
	* @param recipientsToList
	* @param recipientsCcList
	* @param recipientsBccList
	* @param fromMailId
	* @param templateName Template name to compute the subject and body
	* @param context Context map to be used while processing template subject and body
	*/
	public void sendEmail(String recipientsToList[], String recipientsCcList[], String recipientsBccList[], String fromMailId,
			String templateName, Map<String, ?> context)
	{
		EmailTemplate template = this.templateMap.get(templateName);

		if(template == null)
		{
			throw new IllegalArgumentException("Invalid email template name specified: " + templateName);
		}

		// compute subject and body from template
		String subject = template.getSubjectFromTemplate(context);
		String body = template.getBodyFromTemplate(context);

		try
		{
			// Build mail message object
			Message message = buildMessage(recipientsToList, recipientsCcList, recipientsBccList, fromMailId,
					subject, body);

			// send the message
			Transport.send(message);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while sending email", ex);
		}
	}

	/**
	* Service method to send mails. This method accepts the subject and body for the mail to be sent.
	*
	* @param recipientsToList
	* @param recipientsCcList
	* @param recipientsBccList
	* @param fromMailId
	* @param subject
	* @param body
	*/
	public void sendEmail(String recipientsToList[], String recipientsCcList[], String recipientsBccList[], String fromMailId,
			String subject, String body)
	{
		try
		{
			// Build mail message object
			Message message = buildMessage(recipientsToList, recipientsCcList, recipientsBccList, fromMailId,
					subject, body);

			// send the message
			Transport.send(message);
		}catch(Exception ex)
		{
			throw new IllegalStateException("An error occurred while sending email", ex);
		}
	}
	
}
