/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 Arne Plöse
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


import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.request.OneWireRequest;
import de.ibapl.onewire4j.request.communication.OneWireSpeed;
import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;
import java.io.IOException;
import java.util.function.Consumer;

import de.ibapl.spsw.api.SerialPortSocket;

/**
 *
 * @author aploese
 */
public interface OneWireAdapter extends AutoCloseable {
	
	void open() throws IOException;
	
	void setSerialPort(SerialPortSocket serialPort);
	
	boolean isOpen();

	void searchDevices(Consumer<Long> c) throws IOException;
	
	void searchDevices(Consumer<OneWireContainer> d, boolean init) throws IOException;

	OneWireSpeed getSpeedFromBaudrate();
	
	void sendMatchRomRequest(long address) throws IOException ;

	void sendReset() throws IOException;
	
	byte[] sendRawDataRequest(byte[] data) throws IOException;
	
	byte sendByteWithPower(byte b, StrongPullupDuration strongPullupDuration, OneWireSpeed speed) throws IOException;

	byte sendByte(byte b, OneWireSpeed speed) throws IOException;

	<R> R sendCommand(OneWireRequest<R> request) throws IOException;

	void sendCommands(OneWireRequest<?>[] requests) throws IOException;
	
	void sendTerminatePulse() throws IOException;

	void sendSkipRomRequest() throws IOException;

	byte sendReadByteRequest() throws IOException;
	
}
