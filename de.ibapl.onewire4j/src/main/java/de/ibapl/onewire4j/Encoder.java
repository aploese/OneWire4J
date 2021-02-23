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
package de.ibapl.onewire4j;

import java.io.IOException;
import java.io.OutputStream;

import de.ibapl.onewire4j.container.OneWireDataCommand;
import de.ibapl.onewire4j.request.CommandRequest;
import de.ibapl.onewire4j.request.OneWireRequest;
import de.ibapl.onewire4j.request.OneWireRequest.RequestState;
import de.ibapl.onewire4j.request.PulseTerminationRequest;
import de.ibapl.onewire4j.request.communication.CommunicationRequest;
import de.ibapl.onewire4j.request.communication.OneWireSpeed;
import de.ibapl.onewire4j.request.communication.PulseRequest;
import de.ibapl.onewire4j.request.communication.ResetDeviceRequest;
import de.ibapl.onewire4j.request.communication.SearchAcceleratorCommand;
import de.ibapl.onewire4j.request.communication.SingleBitRequest;
import de.ibapl.onewire4j.request.configuration.ConfigurationReadRequest;
import de.ibapl.onewire4j.request.configuration.ConfigurationRequest;
import de.ibapl.onewire4j.request.configuration.ConfigurationWriteRequest;
import de.ibapl.onewire4j.request.configuration.DataSampleOffsetAndWrite0RecoveryTime;
import de.ibapl.onewire4j.request.configuration.LoadSensorThreshold;
import de.ibapl.onewire4j.request.configuration.ProgrammingPulseDuration;
import de.ibapl.onewire4j.request.configuration.PullDownSlewRateParam;
import de.ibapl.onewire4j.request.configuration.SerialPortSpeed;
import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;
import de.ibapl.onewire4j.request.configuration.Write1LowTime;
import de.ibapl.onewire4j.request.data.DataRequest;
import de.ibapl.onewire4j.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire4j.request.data.RawDataRequest;
import de.ibapl.onewire4j.request.data.SearchCommand;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * Encodes a OneWireRequest to a byte or byte[] and write that data to the
 * {@linkplain OutputStream}.
 *
 * @author Arne Plöse
 */
public class Encoder {

    @OneWireDataCommand
    public static final byte MATCH_ROM_CMD = 0x55;
    public static final byte RESET_CMD = (byte) 0xC1;
    @OneWireDataCommand
    public static final byte SKIP_ROM_CMD = (byte) 0xcc;
    public static final byte SWITCH_TO_COMMAND_MODE_BYTE = (byte) 0xe3;
    public static final byte SWITCH_TO_DATA_MODE_BYTE = (byte) 0xe1;
    public final static byte ONE_WIRE_READ_BYTE_FILLER = (byte) 0xff;
    private final byte[] readTimeSlotsCache;

    final ByteBuffer buff;

    /**
     * Create a new instance and set the {@linkplain OutputStream}.
     *
     * @param os the OutputStream to write to.
     */
    public Encoder(ByteBuffer buff) {
        this.buff = buff;
        readTimeSlotsCache = new byte[buff.capacity()];
        Arrays.fill(readTimeSlotsCache, ONE_WIRE_READ_BYTE_FILLER);
    }

    /**
     * Encode and write the request to the OutputStream. Work is done by
     * delegating the actual work to the private methods.
     *
     * @param request the request to encode.
     * @throws IOException if an error happens.
     */
    public <R> void encode(OneWireRequest<R> request) throws IOException {
        request.throwIfNot(RequestState.READY_TO_SEND);

        if (request instanceof CommandRequest) {
            if (request instanceof ConfigurationRequest) {
                if (request instanceof ConfigurationReadRequest) {
                    buff.put(encodeConfigurationReadRequest((ConfigurationReadRequest<R>) request));
                } else if (request instanceof ConfigurationWriteRequest) {
                    buff.put(encodeConfigurationWriteRequest((ConfigurationWriteRequest<R>) request));
                } else {
                    throw new RuntimeException("Unknown subtype of ConfigurationRequest: " + request.getClass());
                }
            } else if (request instanceof CommunicationRequest) {
                if (request instanceof SingleBitRequest) {
                    buff.put(encodeSingleBitSendCommand((SingleBitRequest) request));
                } else if (request instanceof SearchAcceleratorCommand) {
                    buff.put(encodeSearchAcceleratorCommand((SearchAcceleratorCommand) request));
                } else if (request instanceof ResetDeviceRequest) {
                    buff.put(encodeResetDevice((ResetDeviceRequest) request));
                } else if (request instanceof PulseRequest) {
                    buff.put(encodePulseRequest((PulseRequest) request));
                } else if (request instanceof PulseTerminationRequest) {
                    buff.put((byte) 0xF1);
                } else {
                    throw new RuntimeException("Unknown subtype of CommunicationRequest: " + request.getClass());
                }
            } else {
                throw new RuntimeException("Unknown subtype of CommandRequest: " + request.getClass());
            }
        } else if (request instanceof DataRequest) {
            if (request instanceof SearchCommand) {
                buff.put((byte) 0xf0);
            } else if (request instanceof RawDataRequest) {
                writeDataBytes(((RawDataRequest) request).requestData, request.readTimeSlots);
            } else if (request instanceof DataRequestWithDeviceCommand) {
                final DataRequestWithDeviceCommand r = (DataRequestWithDeviceCommand) request;
                buff.put(r.command);
                writeDataBytes(r.requestData, request.readTimeSlots);
            } else {
                throw new RuntimeException("NOT IMPLEMENTED: " + request.getClass());
            }
        } else {
            throw new RuntimeException("Unknown subtype of CommandRequest: " + request.getClass());
        }
        request.waitForResponse();
    }

    private byte encodeConfigurationReadRequest(ConfigurationReadRequest<?> configurationCommand) throws IOException {
        switch (configurationCommand.commandType) {
            case PDSRC:
                return 0b0_000_001_1;
            case PPD:
                return 0b0_000_010_1;
            case SPUD:
                return 0b0_000_011_1;
            case W1LT:
                return 0b0_000_100_1;
            case DSO_AND_W0RT:
                return 0b0_000_101_1;
            case LST:
                return 0b0_000_110_1;
            case RBR:
                return 0b0_000_111_1;
            default:
                throw new RuntimeException("Unknown Configuration command: " + configurationCommand.commandType);
        }
    }

    @SuppressWarnings("unchecked")
    private byte encodeConfigurationWriteRequest(ConfigurationWriteRequest<?> configurationCommand) throws IOException {
        switch (configurationCommand.commandType) {
            case PDSRC:
                switch (((ConfigurationWriteRequest<PullDownSlewRateParam>) configurationCommand).propertyValue) {
                    case PDSRC_15:
                        return 0b0_001_000_1;
                    case PDSRC_2_2:
                        return 0b0_001_001_1;
                    case PDSRC_1_65:
                        return 0b0_001_010_1;
                    case PDSRC_1_37:
                        return 0b0_001_011_1;
                    case PDSRC_1_1:
                        return 0b0_001_100_1;
                    case PDSRC_0_83:
                        return 0b0_001_101_1;
                    case PDSRC_0_7:
                        return 0b0_001_110_1;
                    case PDSRC_0_55:
                        return 0b0_001_111_1;
                    default:
                        throw new RuntimeException("Cant't handle PDSRC: " + configurationCommand.propertyValue);
                }
            case PPD:
                switch (((ConfigurationWriteRequest<ProgrammingPulseDuration>) configurationCommand).propertyValue) {
                    case PPD_32:
                        return 0b0_010_000_1;
                    case PPD_64:
                        return 0b0_010_001_1;
                    case PPD_128:
                        return 0b0_010_010_1;
                    case PPD_256:
                        return 0b0_010_011_1;
                    case PPD_512:
                        return 0b0_010_100_1;
                    case PPD_1024:
                        return 0b0_010_101_1;
                    case PPD_2048:
                        return 0b0_010_110_1;
                    case PPD_POSITIVE_INFINITY:
                        return 0b0_010_111_1;
                    default:
                        throw new RuntimeException("Cant't handle PPD: " + configurationCommand.propertyValue);
                }
            case SPUD:
                switch (((ConfigurationWriteRequest<StrongPullupDuration>) configurationCommand).propertyValue) {
                    case SPUD_16_4:
                        return 0b0_011_000_1;
                    case SPUD_65_5:
                        return 0b0_011_001_1;
                    case SPUD_131:
                        return 0b0_011_010_1;
                    case SPUD_262:
                        return 0b0_011_011_1;
                    case SPUD_524:
                        return 0b0_011_100_1;
                    case SPUD_1048:
                        return 0b0_011_101_1;
                    case SPUD_DYN:
                        return 0b0_011_110_1;
                    case SPUD_POSITIVE_INFINITY:
                        return 0b0_011_111_1;
                    default:
                        throw new RuntimeException("Cant't handle SPUD: " + configurationCommand.propertyValue);
                }
            case W1LT:
                switch (((ConfigurationWriteRequest<Write1LowTime>) configurationCommand).propertyValue) {
                    case W1LT_8:
                        return 0b0_100_000_1;
                    case W1LT_9:
                        return 0b0_100_001_1;
                    case W1LT_10:
                        return 0b0_100_010_1;
                    case W1LT_11:
                        return 0b0_100_011_1;
                    case W1LT_12:
                        return 0b0_100_100_1;
                    case W1LT_13:
                        return 0b0_100_101_1;
                    case W1LT_14:
                        return 0b0_100_110_1;
                    case W1LT_15:
                        return 0b0_100_111_1;
                    default:
                        throw new RuntimeException("Cant't handle W1LT: " + configurationCommand.propertyValue);
                }
            case DSO_AND_W0RT:
                switch (((ConfigurationWriteRequest<DataSampleOffsetAndWrite0RecoveryTime>) configurationCommand).propertyValue) {
                    case DSO_AND_W0RT_3:
                        return 0b0_101_000_1;
                    case DSO_AND_W0RT_4:
                        return 0b0_101_001_1;
                    case DSO_AND_W0RT_5:
                        return 0b0_101_010_1;
                    case DSO_AND_W0RT_6:
                        return 0b0_101_011_1;
                    case DSO_AND_W0RT_7:
                        return 0b0_101_100_1;
                    case DSO_AND_W0RT_8:
                        return 0b0_101_101_1;
                    case DSO_AND_W0RT_9:
                        return 0b0_101_110_1;
                    case DSO_AND_W0RT_10:
                        return 0b0_101_111_1;
                    default:
                        throw new RuntimeException("Cant't handle DSO_AND_W0RT: " + configurationCommand.propertyValue);
                }
            case LST:
                switch (((ConfigurationWriteRequest<LoadSensorThreshold>) configurationCommand).propertyValue) {
                    case LST_1_8:
                        return 0b0_110_000_1;
                    case LST_2_1:
                        return 0b0_110_001_1;
                    case LST_2_4:
                        return 0b0_110_010_1;
                    case LST_2_7:
                        return 0b0_110_011_1;
                    case LST_3_0:
                        return 0b0_110_100_1;
                    case LST_3_3:
                        return 0b0_110_101_1;
                    case LST_3_6:
                        return 0b0_110_110_1;
                    case LST_3_9:
                        return 0b0_110_111_1;
                    default:
                        throw new RuntimeException("Cant't handle LST: " + configurationCommand.propertyValue);
                }
            case RBR:
                switch (((ConfigurationWriteRequest<SerialPortSpeed>) configurationCommand).propertyValue) {
                    case SPS_9_6:
                    case SPS_19_2:
                        return 0b0_111_001_1;
                    case SPS_57_6:
                        return 0b0_111_010_1;
                    case SPS_115_2:
                        return 0b0_111_011_1;
                    case SPS_9_6_I:
                        return 0b0_111_100_1;
                    case SPS_19_2_I:
                        return 0b0_111_101_1;
                    case SPS_57_6_I:
                        return 0b0_111_110_1;
                    case SPS_115_2_I:
                        return 0b0_111_111_1;
                    default:
                        throw new IllegalArgumentException("Cant't handle RBR: " + configurationCommand.propertyValue);
                }
            default:
                throw new IllegalArgumentException("Unknown Configuration command: " + configurationCommand.commandType);
        }
    }

    private byte encodePulseRequest(PulseRequest request) {
        byte data = (byte) 0b111_0_11_0_1;
        switch (request.pulsePower) {
            case PROGRAMMING_PULSE:
                data |= 0b000_1_00_0_0;
                break;
            case STRONG_PULLUP:
                break;
            default:
                throw new RuntimeException();
        }
        switch (request.pulseType) {
            case ARM_AFTER_EVERY_BYTE:
                data |= 0b000_0_00_1_0;
                break;
            case DISARM:
                break;
            default:
                throw new RuntimeException();
        }
        return data;
    }

    private byte encodeResetDevice(ResetDeviceRequest request) {
        return (byte) (encodeSpeed(request.speed) | 0b1100_00_01);
    }

    private byte encodeSearchAcceleratorCommand(SearchAcceleratorCommand request) {
        switch (request.searchAccelerator) {
            case ON:
                return (byte) (encodeSpeed(request.speed) | (byte) 0b1011_00_01);
            case OFF:
                return (byte) (encodeSpeed(request.speed) | 0b1010_00_01);
            default:
                throw new RuntimeException("Unknown search accelerator: " + request.searchAccelerator);
        }
    }

    private byte encodeSingleBitSendCommand(SingleBitRequest request) throws IOException {
        switch (request.dataToSend) {
            case WRITE_0_BIT:
                return (byte) (encodeSpeed(request.speed) | (request.armPowerDelivery ? 0b100_0_00_11 : 0b100_0_00_01));
            case WRITE_1_OR_READ_BIT:
                return (byte) (encodeSpeed(request.speed) | (request.armPowerDelivery ? 0b100_1_00_11 : 0b100_1_00_01));
            default:
                throw new RuntimeException("Unknown dataToSend: " + request.dataToSend);
        }
    }

    private byte encodeSpeed(OneWireSpeed speed) {
        switch (speed) {
            case STANDARD:
                return 0b0000_00_00;
            case FLEX:
                return 0b0000_01_00;
            case OVERDRIVE:
                return 0b0000_10_00;
            case STANDARD_11:
                return 0b0000_11_00;
            default:
                throw new RuntimeException("Unknown speed: " + speed);
        }
    }

    private void writeDataBytes(final byte[] requestData, int readTimeSlots) throws IOException {
        int lastWriteMark = 0;
        for (int i = 0; i < requestData.length; i++) {
            if (requestData[i] == SWITCH_TO_COMMAND_MODE_BYTE) {
                buff.put(requestData, lastWriteMark, i - lastWriteMark);
                buff.put(requestData[i]);
                lastWriteMark = i;
            }
        }
        buff.put(requestData, lastWriteMark, requestData.length - lastWriteMark);
        buff.put(readTimeSlotsCache, 0, readTimeSlots);
    }

    void put(byte b) {
        buff.put(b);
    }

    void writeTo(WritableByteChannel channel) throws IOException {
        buff.flip();
        channel.write(buff);
        buff.clear();
    }

}
