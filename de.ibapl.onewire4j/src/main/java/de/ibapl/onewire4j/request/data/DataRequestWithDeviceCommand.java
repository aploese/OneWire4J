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
package de.ibapl.onewire4j.request.data;

import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;

/**
 *
 * @author Arne Plöse
 */
public class DataRequestWithDeviceCommand extends RawDataRequest {

    /**
     *
     * @param command the command is send first followed by the data
     * @param requestSize the size for the request data
     * @param readTimeSlots the readTimeSlots in bytes
     */
    public DataRequestWithDeviceCommand(byte command, int requestSize, int readTimeSlots) {
        super(requestSize, readTimeSlots);
        this.command = command;
    }

    public DataRequestWithDeviceCommand(byte command, byte[] requestData, byte[] responseReadData) {
        super(requestData, responseReadData);
        this.command = command;
    }

    public DataRequestWithDeviceCommand(byte command, byte[] requestData) {
        super(requestData, EMPTY_BYTE_ARRAY);
        this.command = command;
    }

    public final byte command;

    @Override
    public int responseSize(StrongPullupDuration spd) {
        return super.responseSize(spd) + 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DataRequestWithDeviceCommand(command=0x%02x", command));
        sb.append(", requestState=").append(requestState);
        sb.append(", requestData=[0x");
        for (int i = 0; i < requestData.length; i++) {
            sb.append(String.format("%02x", requestData[i]));
        }
        sb.append("], readTimeSlots=").append(readTimeSlots);
        sb.append(", response=[0x");
        for (int i = 0; i < response.length; i++) {
            sb.append(String.format("%02x", response[i]));
        }
        sb.append("])");
        sb.append(", responseReadData=[0x");
        for (int i = 0; i < responseReadData.length; i++) {
            sb.append(String.format("%02x", responseReadData[i]));
        }
        sb.append("])");
        return sb.toString();
    }
}
