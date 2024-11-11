/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2023-2024, Arne Plöse and individual contributors as indicated
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
import de.ibapl.onewire4j.request.data.ReadBytesRequest;
import de.ibapl.onewire4j.utils.CRC8;
import java.io.IOException;

/**
 *
 * @author Arne Plöse
 */
@DeviceInfo(oneWireName = "DS2438", iButtonName = "")
public class OneWireDevice26 extends OneWireDevice {

    public static class ReadScratchpadRequest extends DataRequestWithDeviceCommand {

        public ReadScratchpadRequest(byte page) {
            super(READ_SCRATCHPAD_CMD, new byte[]{page}, new byte[9]);
        }

    }

    public static class WriteScratchpadRequest extends DataRequestWithDeviceCommand {

        public WriteScratchpadRequest(byte page, byte[] data) {
            super(WRITE_SCRATCHPAD_CMD, new byte[]{page, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]});
        }

    }

    public final static class StatusConfiguration {

        public final static byte IAD = 0x01;
        public final static byte CA = 0x02;
        public final static byte EE = 0x04;
        public final static byte AD = 0x08;
        public final static byte TB = 0x10;
        public final static byte NVB = 0x20;
        public final static byte ADB = 0x40;
        public final static byte X = (byte) 0x80;

    }

    public static class ScratchpadPageXData {

        protected final byte[] data;

        protected ScratchpadPageXData(byte[] data) {
            if (data.length != 9) {
                throw new IllegalArgumentException("length must be 8 bytes for data + 1 byte for crc");
            }
            this.data = data;
        }
    }

    public static class ScratchpadPage0Data extends ScratchpadPageXData {

        public ScratchpadPage0Data(byte[] data) {
            super(data);
        }

        public byte getStatusConfiguration() {
            return data[0];
        }

        public double getTemperature() {
            return (data[2] | (data[1] & 0xFF)) / 256.0; // converts integer to a double data[2] carries the sign
        }

        public double getVoltage() {
            return ((((data[4] & 0xFF) << 8) | (data[3] & 0xFF))) / 100.0; // unsigned
        }

        public double getCurrent(double valueSensResistor) {
            return (((data[6] << 8) | (data[5] & 0xFF))) / (4096 * valueSensResistor); // data[6] carries the sign
        }

        public byte getThreshold() {
            return data[7];
        }

        public void setStatusConfiguration(byte value) {
            data[0] = value;
        }

        public boolean isVDD() {
            return (data[0] & OneWireDevice26.StatusConfiguration.AD) == OneWireDevice26.StatusConfiguration.AD;
        }

        public void setVDD(boolean value) {
            if (value) {
                data[0] |= OneWireDevice26.StatusConfiguration.AD;
            } else {
                data[0] &= ~OneWireDevice26.StatusConfiguration.AD;
            }
        }
    }

    public static class ScratchpadPage1Data extends ScratchpadPageXData {

        public ScratchpadPage1Data(byte[] data) {
            super(data);
        }

        public long getETM() {
            return (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
        }

        public byte getICA() {
            return data[4];
        }

        public short getOffset() {
            return (short) ((data[5] & 0xff) | (data[6] << 8));
        }

        public byte getReserved() {
            return data[7];
        }
    }

    public static class ScratchpadPage2Data extends ScratchpadPageXData {

        public ScratchpadPage2Data(byte[] data) {
            super(data);
        }

        public long getDisconnect() {
            return (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
        }

        public long getEndOfCharge() {
            return (data[0] & 0xff) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 16) | ((data[3] & 0xff) << 24);
        }

    }

    public static class ScratchpadPage7Data extends ScratchpadPageXData {

        public ScratchpadPage7Data(byte[] data) {
            super(data);
        }

        public short getCCA() {
            return (short) ((data[4] & 0xff) | ((data[5] & 0xff) << 8));
        }

        public short getDCA() {
            return (short) ((data[6] & 0xff) | ((data[7] & 0xff) << 8));
        }

    }

    @OneWireDataCommand
    public final static byte CONVERT_T_CMD = (byte) 0x44;

    @OneWireDataCommand
    public final static byte CONVERT_V_CMD = (byte) 0xB4;

    @OneWireDataCommand
    public final static byte RECALL_MEMORY_CMD = (byte) 0xB8;

    @OneWireDataCommand
    public final static byte READ_SCRATCHPAD_CMD = (byte) 0xBE;

    @OneWireDataCommand
    public final static byte WRITE_SCRATCHPAD_CMD = (byte) 0x4E;

    @OneWireDataCommand
    public final static byte COPY_SCRATCHPAD_CMD = (byte) 0x48;

    public final static byte PAGE_0 = 0x00;
    public final static byte PAGE_1 = 0x01;
    public final static byte PAGE_2 = 0x02;
    public final static byte PAGE_3 = 0x03;
    public final static byte PAGE_4 = 0x04;
    public final static byte PAGE_5 = 0x05;
    public final static byte PAGE_6 = 0x06;
    public final static byte PAGE_7 = 0x07;

    public OneWireDevice26(long address) {
        super(address);
    }

    public void sendDoConvertTRequest(OneWireAdapter adapter) throws IOException {
        adapter.sendMatchRomRequest(getAddress());

        adapter.sendCommand(new DataRequestWithDeviceCommand(CONVERT_T_CMD, 0, 0));
        final ReadBytesRequest r = new ReadBytesRequest(1);
        adapter.sendCommand(r);
        while (r.responseReadData[0] != (byte) 0xff) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //no-op
            }
            r.resetState();
            adapter.sendCommand(r);
        }
    }

    public void sendDoConvertVRequest(OneWireAdapter adapter) throws IOException {
        adapter.sendMatchRomRequest(getAddress());

        adapter.sendCommand(new DataRequestWithDeviceCommand(CONVERT_V_CMD, 0, 0));
        final ReadBytesRequest r = new ReadBytesRequest(1);
        adapter.sendCommand(r);
        while (r.responseReadData[0] != (byte) 0xff) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //no-op
            }
            r.resetState();
            adapter.sendCommand(r);
        }
    }

    public void recallMemory(OneWireAdapter adapter, byte page) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(new DataRequestWithDeviceCommand(RECALL_MEMORY_CMD, new byte[]{page}, new byte[0]));
    }

    public void readScratchpad(OneWireAdapter adapter, ReadScratchpadRequest request) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(request.resetState());
        if (CRC8.crc8(request.responseReadData) != 0) {
            throw new IOException("CRC mismatch for: " + getAddressAsString() + " request: " + request);
        }
    }

    private ReadScratchpadRequest getScratchpadPageX(OneWireAdapter adapter, byte page) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(new DataRequestWithDeviceCommand(RECALL_MEMORY_CMD, new byte[]{page}, new byte[0]));
        adapter.sendMatchRomRequest(getAddress());
        final ReadScratchpadRequest request = new ReadScratchpadRequest(page);
        adapter.sendCommand(request);
        if (CRC8.crc8(request.responseReadData) != 0) {
            throw new IOException("CRC mismatch for: " + getAddressAsString() + " @page " + page + " request: " + request);
        }
        return request;
    }

    public ScratchpadPage0Data getScratchpadPage0(OneWireAdapter adapter, boolean doConvertTemp, boolean doConvertVolt) throws IOException {
        if (doConvertTemp) {
            sendDoConvertTRequest(adapter);
        }
        if (doConvertVolt) {
            sendDoConvertVRequest(adapter);
        }
        ReadScratchpadRequest request = getScratchpadPageX(adapter, PAGE_0);
        return new ScratchpadPage0Data(request.responseReadData);
    }

    public ScratchpadPage1Data getScratchpadPage1(OneWireAdapter adapter) throws IOException {
        ReadScratchpadRequest request = getScratchpadPageX(adapter, PAGE_1);
        return new ScratchpadPage1Data(request.responseReadData);
    }

    public ScratchpadPage2Data getScratchpadPage2(OneWireAdapter adapter) throws IOException {
        ReadScratchpadRequest request = getScratchpadPageX(adapter, PAGE_2);
        return new ScratchpadPage2Data(request.responseReadData);
    }

    public ScratchpadPage7Data getScratchpadPage7(OneWireAdapter adapter) throws IOException {
        ReadScratchpadRequest request = getScratchpadPageX(adapter, PAGE_7);
        return new ScratchpadPage7Data(request.responseReadData);
    }

    private void setScratchpadPageX(OneWireAdapter adapter, byte page, ScratchpadPageXData data) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        final WriteScratchpadRequest request = new WriteScratchpadRequest(page, data.data);
        adapter.sendCommand(request);
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(new DataRequestWithDeviceCommand(COPY_SCRATCHPAD_CMD, new byte[]{page}, new byte[0]));
    }

    public void setScratchpadPage0(OneWireAdapter adapter, ScratchpadPage0Data data) throws IOException {
        setScratchpadPageX(adapter, PAGE_0, data);
    }

}
