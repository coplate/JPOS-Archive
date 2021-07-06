/*
* jPOS Project [http://jpos.org]
* Copyright (C) ???????????????????????????????????????
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.company.listener;


import java.io.IOException;

import java.util.Date;
import java.util.Iterator;
import org.jpos.iso.*;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.q2.iso.QServer;
import org.jpos.q2.iso.ChannelAdaptor;	
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;

import org.jdom.Element;
import org.jpos.core.XmlConfigurable;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;

/**
* MUXLessOneShotConnector implements ISORequestListener
* and forward all incoming messages to a given
* destination Channel
* This is a merge of  org.jpos.q2.iso.OneShotChannelAdaptor  and org.jpos.iso.Connector
*
* We also require the forwarding channel adaptor to be MUXLessChannelAdaptor, that creates a channel and adds it to tne NameRegistrar
* so that we may clone it in this connecor
* <p>Example server qbean:</p>

<server class="org.jpos.q2.iso.QServer" logger="Q2" name="test-server">
 <attr name="port" type="java.lang.Integer">24601</attr>
 <channel class="org.jpos.iso.channel.XMLChannel" logger="Q2" packager="org.jpos.iso.packager.XMLPackager">
 </channel>
 <request-listener class="com.company.listener.MUXLessOneShotConnector" logger="Q2">
  <registered-channel-name>Test.Channel.1</registered-channel-name>
  <timeout>60000</timeout>
  <name>test-listener</name>
  <max-connect-attempts>2</max-connect-attempts>
 </request-listener>
</server>

*
* @author <a href="mailto:Matthew.Carlson@gmail.com">Matthew Carlson</a>
* @version V1.0.MDC.1.0 $Revision: 1 $ $Date: 2009-08-12 09:24:43 -0200 (Wed, 12 Aug 2009) $
* @see org.jpos.iso.ISORequestListener
* @see org.jpos.q2.iso.OneShotChannelAdaptor
* @see org.jpos.iso.Connector
*/
public class MUXLessOneShotConnector
implements ISORequestListener, LogSource, XmlConfigurable
{
	String registeredChannelName;
	ISOChannel channel;
	private Logger logger;
	private String realm;
	protected int timeout = 0;
	int maxConnectAttempts;
	
	/* 
	 *	Implement LogSource
	 */
	public void setLogger (Logger logger, String realm) {
		this.logger = logger;
		this.realm  = realm;
	}
	public String getRealm () {
		return realm;
	}
	public Logger getLogger() {
		return logger;
	}

	/**
	* This takes the same '<channel' child as a OneShotChannelAdaptor
	* and has 
	* @attlist:
	* <ul>
	* <li>timeout
	* <li>max-connect-attempts
	* <li>registered-channel-name
	* </ul>
	* @param cfg Configuration
	*/
	public void setConfiguration(Element e) throws ConfigurationException {
	
		
		try {
			
			String s = e.getChildTextTrim ("max-connect-attempts");
       			maxConnectAttempts = (s!=null) ? Integer.parseInt(s) : 3;  // reasonable default
			
			registeredChannelName = e.getChildTextTrim ("registered-channel-name");
			if( registeredChannelName == null ){
				throw new ConfigurationException ("registered-channel-name has not been defined");
			}
			
			
			

		} catch (Exception ex) {
			throw new ConfigurationException(ex.getMessage(), ex);
		}
	}

	
	
	public boolean process (ISOSource source, ISOMsg m) {

		ISOMsg response = null;
		LogEvent evt = new LogEvent (this,"one-shot-request-listener");
		try {
			channel = (ISOChannel) NameRegistrar.get ( registeredChannelName );
			ISOChannel local_channel = (ISOChannel)channel.clone();
			ISOMsg c = (ISOMsg) m.clone();
			for (int i=0; !local_channel.isConnected() 
                                && i<maxConnectAttempts; i++) 
                        {
                            local_channel.reconnect();
                            if (!local_channel.isConnected())
                                ISOUtil.sleep (1000L);
                        }
                        if (local_channel.isConnected()) {
                            local_channel.send ((ISOMsg) c);
                            response = local_channel.receive();
                            local_channel.disconnect();
                            
                        }
			if (response != null) {
				source.send(response);
			}
			
		} catch (ISOException e) {
			evt.addMessage (e);
		} catch (IOException e) {
			evt.addMessage (e);
		}catch( NotFoundException  ex ){
			System.out.println("NotFoundException");
			evt.addMessage (new ConfigurationException (""+registeredChannelName+" has not been registered"));
				
			}
		Logger.log (evt);

		return true;
	}
}
