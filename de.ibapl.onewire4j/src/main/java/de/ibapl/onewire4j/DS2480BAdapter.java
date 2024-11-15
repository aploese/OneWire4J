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

import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.request.CommandRequest;
import de.ibapl.onewire4j.request.OneWireRequest;
import de.ibapl.onewire4j.request.PulseTerminationRequest;
import de.ibapl.onewire4j.request.communication.CommunicationRequest;
import de.ibapl.onewire4j.request.communication.DataToSend;
import de.ibapl.onewire4j.request.communication.OneWireSpeed;
import de.ibapl.onewire4j.request.communication.PulsePower;
import de.ibapl.onewire4j.request.communication.PulseRequest;
import de.ibapl.onewire4j.request.communication.PulseType;
import de.ibapl.onewire4j.request.communication.ResetDeviceRequest;
import de.ibapl.onewire4j.request.communication.ResetDeviceResponse;
import de.ibapl.onewire4j.request.communication.SearchAccelerator;
import de.ibapl.onewire4j.request.communication.SearchAcceleratorCommand;
import de.ibapl.onewire4j.request.communication.SingleBitRequest;
import de.ibapl.onewire4j.request.communication.SingleBitResponse;
import de.ibapl.onewire4j.request.configuration.CommandType;
import de.ibapl.onewire4j.request.configuration.ConfigurationReadRequest;
import de.ibapl.onewire4j.request.configuration.ConfigurationWriteRequest;
import de.ibapl.onewire4j.request.configuration.DataSampleOffsetAndWrite0RecoveryTime;
import de.ibapl.onewire4j.request.configuration.PullDownSlewRateParam;
import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;
import de.ibapl.onewire4j.request.configuration.Write1LowTime;
import de.ibapl.onewire4j.request.data.DataRequest;
import de.ibapl.onewire4j.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire4j.request.data.RawDataRequest;
import de.ibapl.onewire4j.request.data.ReadBytesRequest;
import de.ibapl.onewire4j.request.data.SearchCommand;
import de.ibapl.spsw.api.DataBits;
import de.ibapl.spsw.api.FlowControl;
import de.ibapl.spsw.api.Parity;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.Speed;
import de.ibapl.spsw.api.StopBits;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides an implementation used for accessing the DS2480B.
 *
 * @author Arne Plöse
 */
public class DS2480BAdapter implements OneWireAdapter {

    public enum State {
        UNKNOWN, INITIALIZING, COMMAND, DATA;
    }

    private final static Logger LOG = Logger.getLogger(DS2480BAdapter.class.getCanonicalName());

    private Decoder decoder;
    private Encoder encoder;
    private final SerialPortSocket serialPort;
    private OneWireSpeed speedFromBaudrate = OneWireSpeed.FLEX;
    private State state = State.UNKNOWN;
    public final static int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * Creates a new instance and initialize the serial port with 9600,8,n,1.
     *
     * @param serialPortSocket the {@linkplain SerialPortSocket} to use.
     * @throws java.io.IOException
     */
    public DS2480BAdapter(SerialPortSocket serialPortSocket) throws IOException {
        if (!serialPortSocket.isOpen()) {
            throw new IllegalStateException("serial port is closed");
        }
        this.serialPort = serialPortSocket;
        try {
            serialPort.setSpeed(Speed._9600_BPS);
            serialPort.setDataBits(DataBits.DB_8);
            serialPort.setParity(Parity.NONE);
            serialPort.setStopBits(StopBits.SB_1);
            serialPort.setFlowControl(FlowControl.getFC_NONE());
            serialPort.setTimeouts(100, 1000, 1000);

            encoder = new Encoder(ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE));
            decoder = new Decoder(ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE));
            init();
        } catch (Exception e) {
            //Clean up
            encoder = null;
            decoder = null;
            if (serialPort.isOpen()) {
                serialPort.close();
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        serialPort.close();
    }

    @Override
    public OneWireSpeed getSpeedFromBaudrate() {
        return speedFromBaudrate;
    }

    protected void init() throws IOException {
        // Taken from AN192 Figure 2
        setState(State.INITIALIZING);
        serialPort.sendBreak(2);

        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        readGarbage();
        encoder.put(Encoder.RESET_CMD);
        encoder.writeTo(serialPort);

        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        setState(State.COMMAND);

        sendCommands(ConfigurationWriteRequest.of(PullDownSlewRateParam.PDSRC_1_37),
                ConfigurationWriteRequest.of(Write1LowTime.W1LT_10),
                ConfigurationWriteRequest.of(DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_8),
                ConfigurationReadRequest.of(CommandType.RBR),
                new SingleBitRequest(OneWireSpeed.STANDARD, DataToSend.WRITE_1_OR_READ_BIT, false));

    }

    @Override
    public boolean isOpen() {
        return serialPort.isOpen();
    }

    protected void readGarbage() throws IOException {
        while (serialPort.getInBufferBytesCount() > 0) {
            decoder.read(serialPort, Math.min(serialPort.getInBufferBytesCount(), decoder.capacity()));
        }
    }

    @Override
    public void searchDevices(byte searchCommand, LongConsumer longConsumer) throws IOException {
        final OWSearchIterator searchIterator = new OWSearchIterator();
        byte[] data = new byte[16];
        Arrays.fill(data, (byte) 0);
        final RawDataRequest searchCommandData = new RawDataRequest(data);
        final SearchCommand searchCmd = new SearchCommand(searchCommand);
        while (!searchIterator.isSearchFinished()) {
            sendCommand(ResetDeviceRequest.of(speedFromBaudrate));
            sendCommands(searchCmd.resetState(),
                    SearchAcceleratorCommand.of(SearchAccelerator.ON, speedFromBaudrate),
                    searchCommandData.resetState(),
                    SearchAcceleratorCommand.of(SearchAccelerator.OFF, speedFromBaudrate));

            searchIterator.interpretSearch(searchCommandData);
            // check results
            if (searchIterator.getAddress() == 0xffffffffffffffffL) {
                // nothing found
                return;
            }
            if (!OneWireContainer.isAsddressValid(searchIterator.getAddress())) {
                LOG.log(Level.WARNING, "SearchError! invalid address: {0}",
                        OneWireContainer.addressToString(searchIterator.getAddress()));
            }
            longConsumer.accept(searchIterator.getAddress());
        }
    }

    @Override
    public void searchDevices(byte searchCommand, Consumer<OneWireContainer> consumer) throws IOException {
        searchDevices(searchCommand, (long address) -> {
            final OneWireDevice device = OneWireDevice.fromAdress(address);
            try {
                device.init(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            consumer.accept(device);
        });
    }

    @Override
    public byte sendByte(byte b, OneWireSpeed speed) throws IOException {
        CommandRequest<?>[] requests = new CommandRequest[8];
        for (int i = 0; i < 8; i++) {
            final SingleBitRequest sbr = new SingleBitRequest();
            sbr.dataToSend = (b & 0x01) == 0x01 ? DataToSend.WRITE_1_OR_READ_BIT : DataToSend.WRITE_0_BIT;
            b >>= 1;
            sbr.speed = speed;
            sbr.armPowerDelivery = false;
            requests[i] = sbr;
        }
        sendCommands(requests);
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            final SingleBitResponse sbr = ((SingleBitRequest) requests[i]).response;
            switch (sbr.bitResult) {
                case _O_READ_BACK -> {
                }
                case _1_READ_BACK ->
                    result |= (byte) (0x01 << i);
                default ->
                    throw new RuntimeException("Can't handle sbr.bitResult: " + sbr.bitResult);
            }
        }
        return result;
    }

    // DODO Use Duration??? and Infinite Reset???
    @Override
    public byte sendByteWithPower(byte b, StrongPullupDuration strongPullupDuration, OneWireSpeed speed)
            throws IOException {
        CommandRequest<?>[] requests = new CommandRequest[9];
        requests[0] = ConfigurationWriteRequest.of(strongPullupDuration);
        for (int i = 0; i < 8; i++) {
            final SingleBitRequest sbr = new SingleBitRequest();
            sbr.dataToSend = (b & 0x01) == 0x01 ? DataToSend.WRITE_1_OR_READ_BIT : DataToSend.WRITE_0_BIT;
            b >>= 1;
            sbr.speed = speed;
            sbr.armPowerDelivery = i >= 7;
            requests[i + 1] = sbr;
        }
        sendCommands(requests);
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            final SingleBitResponse sbr = ((SingleBitRequest) requests[i + 1]).response;
            switch (sbr.bitResult) {
                case _O_READ_BACK -> {
                }
                case _1_READ_BACK ->
                    result |= (byte) (0x01 << i);
                default ->
                    throw new RuntimeException("Can't handle sbr.bitResult: " + sbr.bitResult);
            }
        }
        return result;
    }

    @Override
    public <R> R sendCommand(OneWireRequest<R> request) throws IOException {
        readGarbage();
        switch (state) {
            case COMMAND -> {
                if (request instanceof DataRequest) {
                    setState(State.DATA);
                }
            }
            case DATA -> {
                if ((request instanceof CommandRequest) || (request instanceof CommunicationRequest)) {
                    setState(State.COMMAND);
                }
            }
            default ->
                throw new IllegalStateException("Can't hande adapter state: " + state);
        }
        encoder.encode(request);
        encoder.writeTo(serialPort);

        decoder.read(serialPort, request);
        decoder.decode(request);
        return request.response;
    }

    @Override
    public void sendCommands(OneWireRequest<?>... requests) throws IOException {
        readGarbage();
        for (OneWireRequest<?> request : requests) {
            switch (state) {
                case COMMAND -> {
                    if (request instanceof DataRequest) {
                        setState(State.DATA);
                    }
                }
                case DATA -> {
                    if ((request instanceof CommandRequest) || (request instanceof CommunicationRequest)) {
                        setState(State.COMMAND);
                    }
                }
                default ->
                    throw new IllegalStateException("Can't hande adapter state: " + state);
            }
            encoder.encode(request);
        }

        encoder.writeTo(serialPort);

        for (OneWireRequest<?> request : requests) {
//TODO accumulated read but spud ... border Configuration(Read|Write)Request
            decoder.read(serialPort, request);
            decoder.decode(request);
        }
    }

    @Override
    public void sendMatchRomRequest(long address) throws IOException {
        sendReset();
        final DataRequestWithDeviceCommand request = new DataRequestWithDeviceCommand(Encoder.MATCH_ROM_CMD,
                OneWireContainer.arrayOfAddress(address));
        sendCommand(request);

        long result = OneWireContainer.addressOf(request.response);
        if (result != address) {
            throw new IllegalArgumentException("result (" + OneWireDevice.address2String(result) + ") is not adress (" + OneWireDevice.address2String(address) + ")  to match");
        }
    }

    @Override
    public byte[] sendRawDataRequest(byte[] data) throws IOException {
        return sendCommand(new RawDataRequest(data));
    }

    @Override
    public byte sendReadByteRequest() throws IOException {
        ReadBytesRequest r = new ReadBytesRequest(1);
        sendCommand(r);
        return r.responseReadData[0];
    }

    @Override
    public ResetDeviceResponse sendReset() throws IOException {
        return sendCommand(ResetDeviceRequest.of(getSpeedFromBaudrate()));
    }

    @Override
    public byte[] sendSkipRomRequest() throws IOException {
        sendReset();
        final DataRequestWithDeviceCommand request = new DataRequestWithDeviceCommand(Encoder.SKIP_ROM_CMD, 0, 0);
        return sendCommand(request);
    }

    @Override
    public Byte sendTerminatePulse() throws IOException {
        CommandRequest<?>[] requests = new CommandRequest[3];
        requests[0] = new PulseTerminationRequest();
        requests[1] = PulseRequest.of(PulsePower.STRONG_PULLUP, PulseType.DISARM);
        requests[2] = new PulseTerminationRequest();
        sendCommands(requests);
        return (Byte) requests[2].response;
        //TODO check response ???
    }

    /**
     * Set the state and write the switch to bytes as needed
     *
     * @param state
     * @throws IOException
     */
    private void setState(State state) throws IOException {
        final State old = this.state;
        this.state = state;
        if (old == State.INITIALIZING) {
            return;
        }
        switch (state) {
            case COMMAND ->
                encoder.put(Encoder.SWITCH_TO_COMMAND_MODE_BYTE);
            case DATA ->
                encoder.put(Encoder.SWITCH_TO_DATA_MODE_BYTE);
            default -> {
            }
        }
    }

}
