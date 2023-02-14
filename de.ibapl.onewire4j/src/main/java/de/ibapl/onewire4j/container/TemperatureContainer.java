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
import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;
import de.ibapl.onewire4j.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire4j.request.data.ReadBytesRequest;
import de.ibapl.onewire4j.utils.CRC8;
import java.io.IOException;
import java.time.Instant;

/**
 *
 * @author Arne Plöse
 */
public interface TemperatureContainer extends OneWireContainer {

    public class ReadScratchpadRequest extends DataRequestWithDeviceCommand {

        public ReadScratchpadRequest() {
            super(READ_SCRATCHPAD_CMD, 0, 9);
        }

    }

    @OneWireDataCommand
    public final static byte COPY_SCRATCHPAD_CMD = (byte) 0x48;

    @OneWireDataCommand
    public final static byte READ_POWER_SUPPLY_CMD = (byte) 0xb4;

    @OneWireDataCommand
    public final static byte CONVERT_TEMPERATURE_CMD = (byte) 0x44;

    @OneWireDataCommand
    public final static byte READ_SCRATCHPAD_CMD = (byte) 0xbe;

    @OneWireDataCommand
    public final static byte RECALLEE_CMD = (byte) 0xb8;

    @OneWireDataCommand
    public final static byte WRITE_SCRATCHPAD_CMD = (byte) 0x4e;

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
            adapter.sendCommand(new DataRequestWithDeviceCommand(CONVERT_TEMPERATURE_CMD, 0, 0));
            final ReadBytesRequest r = new ReadBytesRequest(1);
            adapter.sendCommand(r);
            while (r.responseReadData[0] != (byte) 0xff) {
                r.resetState();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    //no-op
                }
                adapter.sendCommand(r);
            }
        }
        return ts;
    }

    /**
     * Returns true if at least one temperature device needs parasite power. If
     * at least one device needs parasite power one can not do a bulk
     * conversation and one must the slower device by device approach.
     *
     * @param adapter
     * @return
     * @throws IOException
     * @todo Mask only temp devices?
     */
    public static boolean isAnyTempDeviceUsingParasitePower(OneWireAdapter adapter) throws IOException {
        //TODO Mask only temp devices?
        adapter.sendSkipRomRequest();

        //TODO new format ... is available
        DataRequestWithDeviceCommand readPowerSupplyRequest = new DataRequestWithDeviceCommand(READ_POWER_SUPPLY_CMD, 0, 1);
        adapter.sendCommand(readPowerSupplyRequest);
        return readPowerSupplyRequest.responseReadData[0] != (byte) 0xff;
    }

    //TODO is this working???
    default boolean isUsingParasitePower(OneWireAdapter adapter) throws IOException {
        adapter.sendMatchRomRequest(getAddress());

        //TODO new format ... is available
        DataRequestWithDeviceCommand readPowerSupplyRequest = new DataRequestWithDeviceCommand(READ_POWER_SUPPLY_CMD, 0, 1);
        adapter.sendCommand(readPowerSupplyRequest);
        return readPowerSupplyRequest.responseReadData[0] != (byte) 0xff;
    }

    default void sendDoConvertTRequest(OneWireAdapter adapter, boolean parasitePowerNeeded) throws IOException {
        adapter.sendMatchRomRequest(getAddress());

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
            adapter.sendCommand(new DataRequestWithDeviceCommand(CONVERT_TEMPERATURE_CMD, 0, 0));
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
    }

    default void copyScratchpad(OneWireAdapter adapter, boolean parasitePowerNeeded) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        if (parasitePowerNeeded) {
            adapter.sendByteWithPower(COPY_SCRATCHPAD_CMD, StrongPullupDuration.SPUD_POSITIVE_INFINITY,
                    adapter.getSpeedFromBaudrate());

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            adapter.sendTerminatePulse();
        } else {
            adapter.sendCommand(new DataRequestWithDeviceCommand(COPY_SCRATCHPAD_CMD, 0, 0));
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
    }

    /**
     * Reacall the alarm trigers from EEPROM and places tehm in the scratchpad
     *
     * @param adapter
     * @throws IOException
     */
    default void recallE2(OneWireAdapter adapter) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(new DataRequestWithDeviceCommand(RECALLEE_CMD, 0, 0));
    }

    default void readScratchpad(OneWireAdapter adapter, ReadScratchpadRequest request) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(request.resetState());
        if (CRC8.crc8(request.responseReadData) != 0) {
            throw new IOException("CRC mismatch");
        }
    }

    default void writeScratchpad(OneWireAdapter adapter, double tL, double tH) throws IOException {
        adapter.sendMatchRomRequest(getAddress());
        adapter.sendCommand(new DataRequestWithDeviceCommand(WRITE_SCRATCHPAD_CMD, new byte[]{(byte) tH, (byte) tL}));
    }

    default double convertAndReadTemperature(OneWireAdapter adapter, boolean parasitePowerNeeded) throws IOException {
        sendDoConvertTRequest(adapter, parasitePowerNeeded);
        final ReadScratchpadRequest request = new ReadScratchpadRequest();
        readScratchpad(adapter, request);
        if (isTemperaturePowerOnResetValue(request)) {
            sendDoConvertTRequest(adapter, parasitePowerNeeded);
            readScratchpad(adapter, request);
        }
        return getTemperature(request);
    }

    boolean isTemperaturePowerOnResetValue(ReadScratchpadRequest request);

    double getTemperature(ReadScratchpadRequest request);

}
