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
 * @author Arne Plöse requestData is sent, response is received and if
 * readTimeSlots is > 0 the read data will be placed in responseReadData.
 */
public class RawDataRequest extends DataRequest<byte[]> {

    public final static byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public RawDataRequest(int requestDataSize, int readTimeSlots) {
        super(readTimeSlots);
        requestData = new byte[requestDataSize];
        response = new byte[requestDataSize];
        responseReadData = new byte[readTimeSlots];
    }

    public RawDataRequest(byte[] requestData, byte[] responseReadData) {
        super(responseReadData.length);
        this.requestData = requestData;
        this.response = new byte[requestData.length];
        this.responseReadData = responseReadData;
    }

    public RawDataRequest(byte[] requestData) {
        this(requestData, EMPTY_BYTE_ARRAY);
    }

    public final byte[] requestData;
    public final byte[] responseReadData;

    @Override
    public int responseSize(StrongPullupDuration spd) {
        return response.length + readTimeSlots;
    }

}
