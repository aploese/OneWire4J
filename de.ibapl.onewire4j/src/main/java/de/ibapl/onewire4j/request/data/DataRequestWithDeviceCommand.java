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
package de.ibapl.onewire4j.request.data;

import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;
import java.util.Arrays;

/**
 *
 * @author Arne Plöse
 */
public class DataRequestWithDeviceCommand extends DataRequest<byte[]> {

    /**
     *
     * @param command the command is send first followed by the data
     * @param size the remaining size for the data
     * @param filler the byte with to fill the data section
     */
    public DataRequestWithDeviceCommand(byte command, int size, byte filler) {
        this(command, new byte[size], new byte[size]);
        Arrays.fill(requestData, filler);
    }

    public DataRequestWithDeviceCommand(byte command, byte[] requestData) {
        this(command, requestData, new byte[requestData.length]);
    }

    public DataRequestWithDeviceCommand(byte command, byte[] requestData, byte[] responseArray) {
        this.requestData = requestData;
        this.response = responseArray;
        this.command = command;
    }

    public final byte[] requestData;
    public final byte command;

    @Override
    public int responseSize(StrongPullupDuration spd) {
        return 1 + response.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataRequestWithDeviceCommand(command=").append(command);
        sb.append(", requestState=").append(requestState);
        sb.append(", requestData=[0x");
        for (int i = 0; i < requestData.length; i++) {
            sb.append(String.format("%02x", requestData[i]));
        }
        sb.append("], response=[0x");
        for (int i = 0; i < response.length; i++) {
            sb.append(String.format("%02x", response[i]));
        }
        sb.append("])");
        return sb.toString();
    }
}
