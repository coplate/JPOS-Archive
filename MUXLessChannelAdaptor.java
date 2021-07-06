/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) ????????????????????
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

package com.company.channel;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.FactoryChannel;
import org.jpos.iso.Channel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOClientSocketFactory;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.FilteredChannel;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.q2.iso.ChannelAdaptor;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;

import org.jdom.Element;

/**
 * MuxLessChannelAdaptor provides an unconnected channel that can be cloned in an MuxLessOneShotConnector
 * exchange.
 * 
 * <p>Example qbean:</p>
 * <?xml version="1.0" encoding="UTF-8"?>
 *  <!-- July 28, 2008	V1.0.MDC.1.0	Matthew Carlson		Written.		-->
 *  <channel-adaptor name="comp-muxless-channel" class="com.company.channel.MUXLessChannelAdaptor" logger="CompLogger">
 *   <registered-channel-name>COMP.Channel</registered-channel-name>
 *   <channel class="com.company.channel.Channel" logger="CompLogger" realm="comp-multilink-channel" packager="org.jpos.iso.packager.GenericPackager" >
 *   <property name="packager-config" value="/path/to/config/Packager.xml" />
 *   <property name="host" value="000.000.000.000" />
 *   <property name="port" value="0000" />
 *   </channel>
 *  </channel-adaptor>
 *
 * @author <a href="mailto:Matthew.Carlson@gmail.com">Matthew Carlson</a>
 * @version V1.0.MDC.1.0 $Revision: 1 $ $Date: 2009-08-12 09:24:43 -0200 (Wed, 12 Aug 2009) $
 * @jmx:mbean description="ISOChannel wrapper" 
 *                extends="org.jpos.q2.QBeanSupportMBean"
 */
public class MUXLessChannelAdaptor 
    extends QBeanSupport
    implements MUXLessChannelAdaptorMBean
{
	
    String registeredChannelName;
    Configuration cfg;
    int maxConnectAttempts;
    
   
    public void startService () {
	try{
		
		Element persist = getPersist ();
        	registeredChannelName = persist.getChildTextTrim ("registered-channel-name");
		ISOChannel s = createChannel();
		if( registeredChannelName != null ){
            		NameRegistrar.register (registeredChannelName, s);
		}else{
			getLog().warn ("registered-channel-name Not defined");
		}
		
	}catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
        
        
    }
    public void stopService () {
	if( registeredChannelName != null )
        	NameRegistrar.unregister (registeredChannelName);
	registeredChannelName = null;
    }
    public void destroyService () {
	if( registeredChannelName != null )
        	NameRegistrar.unregister (registeredChannelName);
	registeredChannelName = null;
    }

     public ISOChannel createChannel () throws ConfigurationException {
	    ChannelAdaptor adaptor = new ChannelAdaptor ();
            Element persist = getPersist ();
            Element e = persist.getChild ("channel");
            if (e == null)
                throw new ConfigurationException ("channel element missing");

            ISOChannel channel = adaptor.newChannel (e, getFactory());
            
            String socketFactoryString = getSocketFactory();
            if (socketFactoryString != null && channel instanceof FactoryChannel) {
                ISOClientSocketFactory sFac = (ISOClientSocketFactory) getFactory().newInstance(socketFactoryString);
                if (sFac != null && sFac instanceof LogSource) {
                    ((LogSource) sFac).setLogger(log.getLogger(),getName() + ".socket-factory");
                }
                getFactory().setConfiguration (sFac, e);
                ((FactoryChannel)channel).setSocketFactory(sFac);
            }
		return channel;

        }

    
    
    /**
     * @jmx:managed-attribute description="remote host address"
     */
    public synchronized void setHost (String host) {
        setProperty (getProperties ("channel"), "host", host);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="remote host address"
     */
    public String getHost () {
        return getProperty (getProperties ("channel"), "host");
    }
    /**
     * @jmx:managed-attribute description="remote port"
     */
    public synchronized void setPort (int port) {
        setProperty (
            getProperties ("channel"), "port", Integer.toString (port)
        );
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="remote port"
     */
    public int getPort () {
        int port = 0;
        try {
            port = Integer.parseInt (
                getProperty (getProperties ("channel"), "port")
            );
        } catch (NumberFormatException e) { }
        return port;
    }
    /**
     * @jmx:managed-attribute description="socket factory" 
     */
    public synchronized void setSocketFactory (String sFac) {
        setProperty(getProperties("channel"), "socketFactory", sFac);
        setModified(true);
    }
    /**
     * @jmx:managed-attribute description="socket factory" 
     */
    public String getSocketFactory() {
        return getProperty(getProperties ("channel"), "socketFactory");
    }
}

