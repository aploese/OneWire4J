/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2021, Arne Plöse and individual contributors as indicated
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
@DeviceInfo(oneWireName = "DS18S20", iButtonName = "DS1920")
public class OneWireDevice10 extends OneWireDevice implements TemperatureContainer {

    public OneWireDevice10(long address) {
        super(address);
    }

    @Override
    public double getTemperature(ReadScratchpadRequest request) throws ENotProperlyConvertedException {

        // on some parts, namely the 18S20, you can get invalid readings.
        // basically, the detection is that all the upper 8 bits should
        // be the same by sign extension. the error condition (DS18S20
        // returns 185.0+) violated that condition
        if (((request.response[1] & 0x0ff) != 0x00) && ((request.response[1] & 0x0ff) != 0x0FF)) {
            throw new RuntimeException("Invalid temperature data!");
        }

        int temp = (request.response[0] & 0x0ff) | (request.response[1] << 8);
        if (temp == 0x00aa) {
            // this is the initial value without any conversation done!
            throw new ENotProperlyConvertedException(85);
        }
        temp >>= 1;
        return temp - 0.25 + ((double) request.response[7] - (double) request.response[6]) / request.response[7];
    }

}
