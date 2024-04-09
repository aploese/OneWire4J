/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2024, Arne Plöse and individual contributors as indicated
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

import de.ibapl.onewire4j.request.CommandRequest;
import de.ibapl.onewire4j.request.OneWireRequest;
import de.ibapl.onewire4j.request.OneWireRequest.RequestState;
import de.ibapl.onewire4j.request.PulseTerminationRequest;
import de.ibapl.onewire4j.request.communication.AdapterVersion;
import de.ibapl.onewire4j.request.communication.BitResult;
import de.ibapl.onewire4j.request.communication.CommunicationRequest;
import de.ibapl.onewire4j.request.communication.DataToSend;
import de.ibapl.onewire4j.request.communication.OneWireSpeed;
import de.ibapl.onewire4j.request.communication.PulsePower;
import de.ibapl.onewire4j.request.communication.PulseRequest;
import de.ibapl.onewire4j.request.communication.PulseResponse;
import de.ibapl.onewire4j.request.communication.ResetDeviceRequest;
import de.ibapl.onewire4j.request.communication.ResetDeviceResponse;
import de.ibapl.onewire4j.request.communication.ResetResult;
import de.ibapl.onewire4j.request.communication.SearchAcceleratorCommand;
import de.ibapl.onewire4j.request.communication.SingleBitRequest;
import de.ibapl.onewire4j.request.communication.SingleBitResponse;
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
import de.ibapl.spsw.api.TimeoutIOException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arne Plöse
 */
public class Decoder {

    private final static Logger LOG = Logger.getLogger(Decoder.class.getCanonicalName());

    // This is needed to determine if we must wait or not for
    // StrongPullupDuration.SPUD_POSITIVE_INFINITE
    private StrongPullupDuration spud = StrongPullupDuration.SPUD_524; // TODO DEFAULT??

    private ByteBuffer buff;

    public Decoder() {
    }

    public Decoder(ByteBuffer buff) {
        this.buff = buff;
    }

    public <R> void decode(OneWireRequest<R> request) throws IOException, IllegalArgumentException {
        request.throwIfNot(RequestState.WAIT_FOR_RESPONSE);

        if (request instanceof CommandRequest) {
            if (request instanceof ConfigurationRequest) {
                if (request instanceof ConfigurationReadRequest crr) {
                    decodeConfigurationReadResponse(crr);
                } else if (request instanceof ConfigurationWriteRequest cwr) {
                    decodeConfigurationWriteResponse(cwr);
                } else {
                    throw new IllegalArgumentException("Unknown subtype of ConfigurationRequest: " + request.getClass());
                }
            } else if (request instanceof CommunicationRequest) {
                if (request instanceof SingleBitRequest sbr) {
                    decodeSingleBitResponse(sbr);
                } else if (request instanceof SearchAcceleratorCommand) {
                    request.success();
                } else if (request instanceof ResetDeviceRequest rdr) {
                    decodeResetDeviceResponse(rdr);
                } else if (request instanceof PulseRequest pr) {
                    decodePulseResponse(pr);
                } else if (request instanceof PulseTerminationRequest ptr) {
                    decodePulseTerminationResponse(ptr);
                } else {
                    throw new IllegalArgumentException("Unknown subtype of CommunicationRequest: " + request.getClass());
                }
            } else {
                throw new IllegalArgumentException("Unknown subtype of CommandRequest: " + request.getClass());
            }
        } else if (request instanceof DataRequest) {
            if (request instanceof SearchCommand sc) {
                decodeSearchCommand(sc);
            } else if (request instanceof DataRequestWithDeviceCommand drwdc) {
                decodeDataRequestWithDeviceCommand(drwdc);
            } else if (request instanceof RawDataRequest rdr) {
                decodeRawDataRequest(rdr);
            } else {
                throw new IllegalArgumentException("NOT IMPLEMENTED: " + request.getClass());
            }
        } else {
            throw new IllegalArgumentException("Unknown subtype of CommandRequest: " + request.getClass());
        }
    }

    private void decodeSearchCommand(SearchCommand request) throws IOException {
        final byte b = buff.get();
        ((SearchCommand) request).response = b == 0xF0;
        request.success();
    }

    private void decodeRawDataRequest(RawDataRequest request) throws IOException {
        if (buff.remaining() < request.response.length) {
            throw new RuntimeException("ERR response");
        }
        buff.get(request.response);
        if (buff.remaining() < request.responseReadData.length) {
            throw new RuntimeException("ERR responseReadData");
        }
        buff.get(request.responseReadData);
        request.success();
    }

    private void decodeDataRequestWithDeviceCommand(DataRequestWithDeviceCommand request) throws IOException {
        final byte c = buff.get();
        if (request.command != c) {
            throw new IllegalArgumentException(String.format("Wrong command: 0x%02x expected: 0x%02x", c, request.command));
        }
        decodeRawDataRequest(request);
    }

    private void decodePulseResponse(PulseRequest request) {
        final PulseResponse response = new PulseResponse();
        if (spud != StrongPullupDuration.SPUD_POSITIVE_INFINITY) {
            final byte b = buff.get();
            if ((b & 0b111_0_11_00) != 0b111_0_11_00) {
                throw new RuntimeException("Wrong Pulse Response expected 0b111_p_11_xx but got: 0b" + Integer.toBinaryString(b & 0xff));
            }
            if (((b & 0b111_1_11_00) != 0b111_1_11_00)) {
                response.pulsePower = PulsePower.PROGRAMMING_PULSE;
            } else {
                response.pulsePower = PulsePower.STRONG_PULLUP;
            }
        }

        response.strongPullupDuration = spud;
        request.response = response;
        request.success();
    }

    private void decodePulseTerminationResponse(PulseTerminationRequest request) {
        final byte b = buff.get();
        if ((b & 0b111111_00) != 0b111011_00) {
            // some adapters return 0xf1
            if (b != (byte) 0b11110001) {
                //TODO if not armed then we got here a 0b10010011 as response of 0xf1 request ??
                throw new RuntimeException("Wrong Pulse Termination Response expected 0b111011xx but got: 0b" + Integer.toBinaryString(b & 0xff));
            }
        }
        request.response = (byte) b;
        request.success();
    }

    private void decodeResetDeviceResponse(ResetDeviceRequest request) {
        final byte b = buff.get();
        if ((b & 0b110_0_00_00) != 0b110_0_00_00) {
            throw new RuntimeException("No ResetDeviceResponse: 0x" + Integer.toHexString(b));
        }
        final ResetDeviceResponse response = new ResetDeviceResponse();
        response.adapterVersion = switch (b & 0b00_0_111_00) {
            case 0b00_0_000_00 ->
                AdapterVersion.UNKNOWN_0;
            case 0b00_0_001_00 ->
                AdapterVersion.UNKNOWN_1;
            case 0b00_0_010_00 ->
                AdapterVersion.DS2480;
            case 0b00_0_011_00 ->
                AdapterVersion.DS2480B;
            case 0b00_0_100_00 ->
                AdapterVersion.UNKNOWN_4;
            case 0b00_0_101_00 ->
                AdapterVersion.UNKNOWN_5;
            case 0b00_0_110_00 ->
                AdapterVersion.UNKNOWN_6;
            case 0b00_0_111_00 ->
                AdapterVersion.UNKNOWN_7;
            default ->
                throw new RuntimeException("Unknown adapter version: 0x" + Integer.toHexString(b));
        };

        response.resetresult = switch (b & 0b00_0_000_11) {
            case 0b00_0_000_00 ->
                ResetResult._1_WIRE_SHORTED;
            case 0b00_0_000_01 ->
                ResetResult.PRESENCE;
            case 0b00_0_000_10 ->
                ResetResult.ALARM_PRESENCE;
            case 0b00_0_000_11 ->
                ResetResult.NO_PRESENCE;
            default ->
                throw new RuntimeException("Unknown reset result: 0x" + Integer.toHexString(b));
        };
        request.response = response;
        request.success();
    }

    private void decodeSingleBitResponse(SingleBitRequest request) {
        final byte b = buff.get();
        if ((b & 0b111_0_00_00) != 0b100_0_00_00) {
            throw new IllegalArgumentException("No SingleBitResponse: 0x" + Integer.toHexString(b));
        }
        final SingleBitResponse response = new SingleBitResponse();
        if ((b & 0b000_1_00_00) == 0b000_1_00_00) {
            response.dataToSend = DataToSend.WRITE_1_OR_READ_BIT;
        } else {
            response.dataToSend = DataToSend.WRITE_0_BIT;
        }
        response.speed = switch (b & 0b000_0_11_00) {
            case 0b000_0_00_00 ->
                OneWireSpeed.STANDARD;
            case 0b000_0_01_00 ->
                OneWireSpeed.FLEX;
            case 0b000_0_10_00 ->
                OneWireSpeed.OVERDRIVE;
            case 0b000_0_11_00 ->
                OneWireSpeed.STANDARD_11;
            default ->
                throw new IllegalArgumentException("Unknown speed: 0x" + Integer.toHexString(b));
        };
        response.bitResult = switch (b & 0b000_0_00_11) {
            case 0b000_0_00_00 ->
                BitResult._O_READ_BACK;
            case 0b000_0_00_11 ->
                BitResult._1_READ_BACK;
            default ->
                throw new IllegalArgumentException("Unknown bit result: 0x" + Integer.toHexString(b));
        };
        request.response = response;
        request.success();
    }

    @SuppressWarnings("unchecked")
    private <R> void decodeConfigurationReadResponse(ConfigurationReadRequest<R> request) {
        switch (request.commandType) {
            case PDSRC -> {
                request.response = (R) decodePDSRC(buff.get());
            }
            case PPD -> {
                request.response = (R) decodePPD(buff.get());
            }
            case SPUD -> {
                this.spud = decodeSPUD(buff.get());
                request.response = (R) this.spud;
            }
            case W1LT -> {
                request.response = (R) decodeW1LT(buff.get());
            }
            case DSO_AND_W0RT -> {
                request.response = (R) decodeDSO_AND_W0RT(buff.get());
            }
            case LST -> {
                request.response = (R) decodeLST(buff.get());
            }
            case RBR -> {
                request.response = (R) decodeRBR(buff.get());
            }
            default ->
                throw new IllegalArgumentException("Cant handle configuration read response value: " + request.commandType);
        }
        request.success();
    }

    @SuppressWarnings("unchecked")
    private <R> void decodeConfigurationWriteResponse(ConfigurationWriteRequest<R> request) {
        switch (request.commandType) {
            case PDSRC -> {
                request.response = (R) decodePDSRC(buff.get());
            }
            case PPD -> {
                request.response = (R) decodePPD(buff.get());
            }
            case SPUD -> {
                this.spud = decodeSPUD(buff.get());
                request.response = (R) this.spud;
            }
            case W1LT -> {
                request.response = (R) decodeW1LT(buff.get());
            }
            case DSO_AND_W0RT -> {
                request.response = (R) decodeDSO_AND_W0RT(buff.get());
            }
            case LST -> {
                request.response = (R) decodeLST(buff.get());
            }
            case RBR -> {
                request.response = (R) decodeRBR(buff.get());
            }
            default ->
                throw new IllegalArgumentException("Cant handle configuration write response value: " + request.commandType);
        }
        request.success();
    }

    private ProgrammingPulseDuration decodePPD(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x20) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No PPD: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                ProgrammingPulseDuration.PPD_32;
            case 0x02 ->
                ProgrammingPulseDuration.PPD_64;
            case 0x04 ->
                ProgrammingPulseDuration.PPD_128;
            case 0x06 ->
                ProgrammingPulseDuration.PPD_256;
            case 0x08 ->
                ProgrammingPulseDuration.PPD_512;
            case 0x0A ->
                ProgrammingPulseDuration.PPD_1024;
            case 0x0C ->
                ProgrammingPulseDuration.PPD_2048;
            case 0x0E ->
                ProgrammingPulseDuration.PPD_POSITIVE_INFINITY;
            default ->
                throw new IllegalArgumentException("Cant handle PPD byte value: " + b);
        };
    }

    private PullDownSlewRateParam decodePDSRC(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x10) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No PDSRC: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                PullDownSlewRateParam.PDSRC_15;
            case 0x02 ->
                PullDownSlewRateParam.PDSRC_2_2;
            case 0x04 ->
                PullDownSlewRateParam.PDSRC_1_65;
            case 0x06 ->
                PullDownSlewRateParam.PDSRC_1_37;
            case 0x08 ->
                PullDownSlewRateParam.PDSRC_1_1;
            case 0x0A ->
                PullDownSlewRateParam.PDSRC_0_83;
            case 0x0C ->
                PullDownSlewRateParam.PDSRC_0_7;
            case 0x0E ->
                PullDownSlewRateParam.PDSRC_0_55;
            default ->
                throw new IllegalArgumentException("Cant handle PDSRC byte value: " + b);
        };
    }

    private StrongPullupDuration decodeSPUD(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x30) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No SPUD: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                StrongPullupDuration.SPUD_16_4;
            case 0x02 ->
                StrongPullupDuration.SPUD_65_5;
            case 0x04 ->
                StrongPullupDuration.SPUD_131;
            case 0x06 ->
                StrongPullupDuration.SPUD_262;
            case 0x08 ->
                StrongPullupDuration.SPUD_524;
            case 0x0A ->
                StrongPullupDuration.SPUD_1048;
            case 0x0C ->
                StrongPullupDuration.SPUD_DYN;
            case 0x0E ->
                StrongPullupDuration.SPUD_POSITIVE_INFINITY;
            default ->
                throw new IllegalArgumentException("Cant handle SPUD byte value: " + b);
        };
    }

    private Write1LowTime decodeW1LT(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x40) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No W1LT: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                Write1LowTime.W1LT_8;
            case 0x02 ->
                Write1LowTime.W1LT_9;
            case 0x04 ->
                Write1LowTime.W1LT_10;
            case 0x06 ->
                Write1LowTime.W1LT_11;
            case 0x08 ->
                Write1LowTime.W1LT_12;
            case 0x0A ->
                Write1LowTime.W1LT_13;
            case 0x0C ->
                Write1LowTime.W1LT_14;
            case 0x0E ->
                Write1LowTime.W1LT_15;
            default ->
                throw new IllegalArgumentException("Cant handle W1LT byte value: " + b);
        };
    }

    private DataSampleOffsetAndWrite0RecoveryTime decodeDSO_AND_W0RT(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x50) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No DSO_AND_W0RT: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_3;
            case 0x02 ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_4;
            case 0x04 ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_5;
            case 0x06 ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_6;
            case 0x08 ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_7;
            case 0x0A ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_8;
            case 0x0C ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_9;
            case 0x0E ->
                DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_10;
            default ->
                throw new IllegalArgumentException("Cant handle DSO_AND_W0RT byte value: " + b);
        };
    }

    private LoadSensorThreshold decodeLST(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x60) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No LST: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                LoadSensorThreshold.LST_1_8;
            case 0x02 ->
                LoadSensorThreshold.LST_2_1;
            case 0x04 ->
                LoadSensorThreshold.LST_2_4;
            case 0x06 ->
                LoadSensorThreshold.LST_2_7;
            case 0x08 ->
                LoadSensorThreshold.LST_3_0;
            case 0x0A ->
                LoadSensorThreshold.LST_3_3;
            case 0x0C ->
                LoadSensorThreshold.LST_3_6;
            case 0x0E ->
                LoadSensorThreshold.LST_3_9;
            default ->
                throw new IllegalArgumentException("Cant handle LST byte value: " + b);
        };
    }

    private SerialPortSpeed decodeRBR(final byte b) throws RuntimeException {
        if (((b & 0xF0) != 0x70) && ((b & 0xF0) != 0x00)) {
            throw new IllegalArgumentException("No RBR: 0x" + Integer.toHexString(b));
        }
        return switch (b & 0x0F) {
            case 0x00 ->
                SerialPortSpeed.SPS_9_6;
            case 0x02 ->
                SerialPortSpeed.SPS_19_2;
            case 0x04 ->
                SerialPortSpeed.SPS_57_6;
            case 0x06 ->
                SerialPortSpeed.SPS_115_2;
            case 0x08 ->
                SerialPortSpeed.SPS_9_6_I;
            case 0x0A ->
                SerialPortSpeed.SPS_19_2_I;
            case 0x0C ->
                SerialPortSpeed.SPS_57_6_I;
            case 0x0E ->
                SerialPortSpeed.SPS_115_2_I;
            default ->
                throw new IllegalArgumentException("Cant handle RBR byte value: " + b);
        };
    }

    public void read(ReadableByteChannel channel, int len) throws IOException {
        buff.position(0);
        buff.limit(len);
        channel.read(buff);
        buff.flip();
    }

    public void read(ReadableByteChannel channel, OneWireRequest<?> request) throws IOException {
        buff.position(0);
        buff.limit(request.responseSize(spud));
        try {
            channel.read(buff);
        } catch (TimeoutIOException tioe) {
            LOG.log(Level.SEVERE, "Timeout during read response of: {0} expected length: {1}", new Object[]{request, request.responseSize(spud)});
            throw tioe;
        }
        buff.flip();
    }

    public int capacity() {
        return buff.capacity();
    }

}
