/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 - 2018 Arne Plöse
 * %%
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2009, 2017, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
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
 * #L%
 */
package de.ibapl.onewire4j;

import java.io.IOException;

import de.ibapl.spsw.api.SerialPortSocket;

/**
 * Factory to choose a implementation {@link OneWireAdapter}.
 * 
 * @author Arne Plöse
 */
public class AdapterFactory {

	/**
	 * Choose and create an adapter, open the port and return the opened adapter.
	 * 
	 * 
	 * @param serialPortSocketFactory
	 *            the factory to use.
	 * @param portname
	 *            the name of the port to open.
	 * @return the created and opened adapter.
	 * @throws IOException
	 *             on error.
	 */
	public OneWireAdapter open(SerialPortSocket serialPortSocket) throws IOException {
		final DS2480BAdapter result = new DS2480BAdapter(serialPortSocket);
		result.open();
		return result;
	}
}
