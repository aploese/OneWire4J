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

/**
 *
 * @author Arne Plöse
 */
@DeviceInfo(oneWireName="DS18B20,DS1820B,DS18B20X", iButtonName="")
public class OneWireDevice28 extends OneWireDevice implements TemperatureContainer {

	public OneWireDevice28(long address) {
		super(address);
	}

	@Override
	public double getTemperature(ReadScratchpadRequest request) throws ENotProperlyConvertedException {
		final int intTemperature = (request.response[0] & 0xFF) | (request.response[1] << 8); // this converts 2 bytes into
		if (intTemperature == 0x0550) {
			// Sometimes 85°C appears maybe a hidden conversation error??
			throw new ENotProperlyConvertedException(85);
		}
		final double result = intTemperature / 16.0; // converts integer to a double
		return (result);
	}

}
