/**
 * Copyright 2012-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Andrew Tikhonov <andrew.tikhonov@gmail.com>
 **/
package service.util;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;
import java.util.Properties;


/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 29/11/2013
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class MailClient {

	private boolean    _debug = false;
	private Properties _props = null;

	private void initprops() { _props = System.getProperties(); }

	public MailClient()              { initprops(); _props.put( "mail.smtp.host", "smtp.ebi.ac.uk" ); }
	public MailClient(String server) { initprops(); _props.put( "mail.smtp.host", server ); }


	public void sendMail(String from, String to[], String subject, String body, String[] attachments) throws Exception {
		int i;

		// get a mail session
		Session session = Session.getDefaultInstance( _props, null );
		session.setDebug(_debug);

		// define a new message
		Message msg = new MimeMessage( session );
		msg.setFrom(new InternetAddress( from ));

		InternetAddress[] addressTo = new InternetAddress[to.length];
		for(i = 0; i < to.length; i++) {
			addressTo[i] = new InternetAddress( to[i] );
		}
		msg.addRecipients( Message.RecipientType.TO, addressTo );

		// msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		msg.setSubject( subject );

		// create a message body part
		MimeBodyPart msgBody = new MimeBodyPart();

		//msgBody.setText(body);
		msgBody.setContent( body, "text/html" );

		// multipart for attachments (just in case)
		Multipart mp = new MimeMultipart();

		// add the body part
		mp.addBodyPart( msgBody );


		for(i = 0; i < attachments.length; i++) {

			String fname = attachments[i];
			MimeBodyPart attBodyPart = new MimeBodyPart();

			//use a JAF FileDataSource as it does MIME type detection
			DataSource source = new FileDataSource( fname );
			attBodyPart.setDataHandler(new DataHandler( source ));

			// set the name of the file
			attBodyPart.setFileName( source.getName() );

			// add the attachment
			mp.addBodyPart( attBodyPart );
		}

		msg.setContent( mp );

		Transport.send( msg );

	}
}

