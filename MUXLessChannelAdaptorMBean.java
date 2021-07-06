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

/**
 * MBean interface.
* @author <a href="mailto:Matthew.Carlson@gmail.com">Matthew Carlson</a>
* @version V1.0.MDC.1.0 $Revision: 1 $ $Date: 2009-08-12 09:24:43 -0200 (Wed, 12 Aug 2009) $
* @see org.jpos.iso.ISORequestListener
* @see org.jpos.q2.iso.OneShotChannelAdaptor
* @see org.jpos.iso.Connector
*/
 */
public interface MUXLessChannelAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setHost(java.lang.String host) ;

  java.lang.String getHost() ;

  void setPort(int port) ;

  int getPort() ;

  void setSocketFactory(java.lang.String sFac) ;

  java.lang.String getSocketFactory() ;

}
