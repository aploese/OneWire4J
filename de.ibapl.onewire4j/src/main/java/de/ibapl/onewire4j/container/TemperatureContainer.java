/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2019, Arne Plöse and individual contributors as indicated
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
 */
package de.ibapl.onewire4j.container;

import java.io.IOException;
import java.time.Instant;

import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;
import de.ibapl.onewire4j.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire4j.request.data.ReadBytesRequest;
import de.ibapl.onewire4j.utils.CRC8;

/**
 *
 * @author Arne Plöse
 */
public interface TemperatureContainer extends OneWireContainer {

	@OneWireDataCommand
	public final byte READ_POWER_SUPPLY_CMD = (byte) 0xb4;

	@OneWireDataCommand
	public final byte CONVERT_TEMPERATURE_CMD = (byte) 0x44;

	/**
	 * Sends convert to all devices....
	 */
	public static Instant sendDoConvertRequestToAll(OneWireAdapter adapter, boolean parasitePowerNeeded)
			throws IOException {
		adapter.sendSkipRomRequest();
		final Instant ts = Instant.now();

		if (parasitePowerNeeded) {
			adapter.sendByteWithPower(CONVERT_TEMPERATURE_CMD, StrongPullupDuration.SPUD_POSITIVE_INFINITY,
					adapter.getSpeedFromBaudrate());
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			adapter.sendTerminatePulse();
		} else {
			adapter.sendCommand(new DataRequestWithDeviceCommand(CONVERT_TEMPERATURE_CMD, new byte[0]));
			final ReadBytesRequest r = new ReadBytesRequest(1);
			while (adapter.sendCommand(r)[0] != (byte) 0xff) {
				r.resetState();
				try {
					Thread.sleep(100);
				} catch (InterruptedException  e) {
					//no-op
				}
			}
		}
		return ts;
	}

	/**
	 * Returns true if at least one temperature device needs parasite power.
	 * If at least one device needs parasite power one can not do a bulk conversation and one must the slower device by device approach.
	 * 
	 * @param adapter
	 * @return
	 * @throws IOException
	 */
	public static boolean isParasitePower(OneWireAdapter adapter) throws IOException {
		adapter.sendSkipRomRequest();
		
		DataRequestWithDeviceCommand readPowerSupplyRequest = new DataRequestWithDeviceCommand(READ_POWER_SUPPLY_CMD,
				new byte[] { (byte) 0xff });
		adapter.sendCommand(readPowerSupplyRequest);
		return readPowerSupplyRequest.response[0] != (byte) 0xff;
	}

	default void sendDoConvertRequest(OneWireAdapter adapter) throws IOException {
		adapter.sendMatchRomRequest(getAddress());

		adapter.sendByteWithPower(CONVERT_TEMPERATURE_CMD, StrongPullupDuration.SPUD_POSITIVE_INFINITY,
				adapter.getSpeedFromBaudrate());

		try {
			Thread.sleep(750);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		adapter.sendTerminatePulse();
		// TODO READ Bit???
		byte b = adapter.sendReadByteRequest();
		if ((b & 0xff) != 0xFF) {
			throw new RuntimeException();
		}
	}

	default void readScratchpad(OneWireAdapter adapter, ReadScratchpadRequest request) throws IOException {
		adapter.sendMatchRomRequest(getAddress());
		adapter.sendCommand(request.resetState());
		if (CRC8.computeCrc8(request.response) != 0) {
			throw new IOException("CRC mismatch");
		}
	}

	default double convertAndReadTemperature(OneWireAdapter adapter)
			throws IOException, ENotProperlyConvertedException {
		final ReadScratchpadRequest request = new ReadScratchpadRequest();
		sendDoConvertRequest(adapter);
		readScratchpad(adapter, request);
		return getTemperature(request);
	}

	default double getTemperature(ReadScratchpadRequest request) throws ENotProperlyConvertedException {
		return request.response[2];
	}

	default double getAlarmTempLowerLimit(ReadScratchpadRequest request) throws ENotProperlyConvertedException {
		return request.response[3];
	}

}
