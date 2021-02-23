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

import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire4j.request.data.RawDataRequest;
import de.ibapl.onewire4j.utils.CRC16;
import java.io.IOException;
import java.util.Arrays;

public interface MemoryBankContainer extends OneWireContainer {

    public class ReadScratchpadRequest extends DataRequestWithDeviceCommand {

        @OneWireDataCommand
        public final static byte READ_SCRATCHPAD_CMD = (byte) 0xaa;

        public ReadScratchpadRequest() {
            super(READ_SCRATCHPAD_CMD, 0, 13);
        }

        public short getAddressFromResponse() {
            return (short) (((response[1] & 0x00ff) << 8) | (response[0] & 0xff));
        }

        public boolean isAA() {
            return (response[2] & 0x80) == 0x80;
        }

        public boolean isPF() {
            return (response[2] & 0x20) == 0x20;
        }

        public byte getEndingAddress() {
            return (byte) (response[2] & 0x07);
        }
    }

    public class WriteScratchpadRequest extends DataRequestWithDeviceCommand {

        @OneWireDataCommand
        public final static byte WRITE_SCRATCHPAD_CMD = (byte) 0x0f;

        private final static int REQUEST_DATA_OFFSET = 2;

        public WriteScratchpadRequest() {
            super(WRITE_SCRATCHPAD_CMD, 10, 2);
        }

        public void setAddress(int address) {
            requestData[1] = (byte) (address >>> 8);
            requestData[0] = (byte) (address & 0x00FF);
        }

        public void setData(byte[] data, int from) {
            //TODO length
            for (int i = 0; i < 8; i++) {
                requestData[i + REQUEST_DATA_OFFSET] = data[i + from];
            }
        }

        public short getAddressFromResponse() {
            return (short) (((response[1] & 0x00ff) << 8) | (response[0] & 0xff));
        }

        public byte[] getCRC() {
            return Arrays.copyOfRange(response, response.length - 2, response.length);
        }

    }

    public class CopyScratchpadRequest extends DataRequestWithDeviceCommand {

        @OneWireDataCommand
        public final static byte COPY_SCRATCHPAD_CMD = (byte) 0x55;

        public CopyScratchpadRequest() {
            super(COPY_SCRATCHPAD_CMD, 3, 0);
        }

        private void setAuthorizationKey(ReadScratchpadRequest readRequest) {
            requestData[0] = readRequest.response[0];
            requestData[1] = readRequest.response[1];
            requestData[2] = readRequest.response[2];
        }
    }

    public class ReadMemoryRequest extends DataRequestWithDeviceCommand {

        @OneWireDataCommand
        public final static byte READ_MEMORY_CMD = (byte) 0xf0;

        public ReadMemoryRequest(int length) {
            super(READ_MEMORY_CMD, 2, length);
        }

        public void setAddress(int address) {
            requestData[1] = (byte) (address >>> 8);
            requestData[0] = (byte) (address & 0x00FF);
        }

        private byte[] getResponseData() {
            return Arrays.copyOfRange(response, 2, response.length);
        }

    }

    default public boolean writeToMemory(OneWireAdapter adapter, int startAddress, byte[] data, int from, int to) throws IOException {
//TODO only once ?? adapter.sendMatchRomRequest(getAddress());
        adapter.sendMatchRomRequest(getAddress());

        final WriteScratchpadRequest writeRequest = new WriteScratchpadRequest();
        writeRequest.setAddress(startAddress);
        writeRequest.setData(data, from);
        adapter.sendCommand(writeRequest);

        CRC16 crc16 = new CRC16();
        crc16.crc16(writeRequest.command);
        crc16.crc16(writeRequest.requestData);
        crc16.crc16(writeRequest.getCRC());
        if (!crc16.isOneComplement()) {
            throw new IOException("CRC mismatch write");
        }

        crc16.resetCurrentCrc16();

        final ReadScratchpadRequest readRequest = new ReadScratchpadRequest();
        //  TODO send resume
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(readRequest);
        if (readRequest.isPF()) {
            throw new IllegalArgumentException("scratchpad is not valid"); // TODO figure out whats wrong ...
        } else if (readRequest.isAA()) {
            throw new IllegalArgumentException("device did not recocnize write command"); // TODO figure out whats wrong ...
        }

        crc16.crc16(readRequest.command);
        crc16.crc16(readRequest.response);
        if (!crc16.isOneComplement()) {
            throw new IOException("CRC mismatch read");
        }

        final CopyScratchpadRequest copyScratchpadRequest = new CopyScratchpadRequest();
        adapter.sendMatchRomRequest(getAddress());
        copyScratchpadRequest.setAuthorizationKey(readRequest);
        adapter.sendCommand(copyScratchpadRequest);

        RawDataRequest rdr = new RawDataRequest(0, 16);

        for (int i = 0; i < 100; i++) {
            rdr.resetState();
            adapter.sendCommand(rdr);
            if (((rdr.response[15] == (byte) 0x55)) || (rdr.response[15] == (byte) 0xaa)) {
                break;
            }
        }

        return true;
    }

    default byte[] readMemory(OneWireAdapter adapter, int address, int len) throws IOException {
        ReadMemoryRequest rm = new ReadMemoryRequest(len);
        rm.setAddress(address);
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(rm);
        return rm.getResponseData();
    }

}
