package de.ibapl.onewire4j;

/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 Arne Plöse
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

import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.request.CommandRequest;
import de.ibapl.onewire4j.request.OneWireRequest;
import de.ibapl.onewire4j.request.PulseTerminationRequest;
import de.ibapl.onewire4j.request.communication.CommunicationRequest;
import de.ibapl.onewire4j.request.communication.DataToSend;
import de.ibapl.onewire4j.request.communication.PulsePower;
import de.ibapl.onewire4j.request.communication.PulseRequest;
import de.ibapl.onewire4j.request.communication.PulseType;
import de.ibapl.onewire4j.request.communication.ResetDeviceRequest;
import de.ibapl.onewire4j.request.communication.SearchAccelerator;
import de.ibapl.onewire4j.request.communication.SearchAcceleratorCommand;
import de.ibapl.onewire4j.request.communication.SingleBitRequest;
import de.ibapl.onewire4j.request.communication.SingleBitResponse;
import de.ibapl.onewire4j.request.communication.Speed;
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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import de.ibapl.spsw.api.Baudrate;
import de.ibapl.spsw.api.DataBits;
import de.ibapl.spsw.api.FlowControl;
import de.ibapl.spsw.api.Parity;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.StopBits;

/**
 *
 * @author aploese
 */
public class DS2480BAdapter implements OneWireAdapter {

	public enum State {
		UNKNOWN, INITIALIZING, COMMAND, DATA;
	}

	private SerialPortSocket serialPort;
	private InputStream is;
	private OutputStream os;
	private State state = State.UNKNOWN;
	private Encoder encoder;
	private Decoder decoder;
    private Speed speedFromBaudrate = Speed.FLEX;
	
	public Speed getSpeedFromBaudrate() {
		return speedFromBaudrate;
	}
	
	/**
	 * Set the state and write the switch to bytes as needed
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
		case COMMAND:
			os.write(Encoder.SWITCH_TO_COMMAND_MODE_BYTE);
			break;
		case DATA:
			os.write(Encoder.SWITCH_TO_DATA_MODE_BYTE);
			break;
		default:
			break;
		}
	}

	@Override
	public <R> R sendCommand(OneWireRequest<R> request) throws IOException {
		readGarbage();
		switch (state) {
		case COMMAND:
			if (request instanceof DataRequest) {
				setState(State.DATA);
			}
			break;
		case DATA:
			if ((request instanceof CommandRequest) || (request instanceof CommunicationRequest)) {
				setState(State.COMMAND);
			}
			break;
		default:
			throw new RuntimeException("Can't hande request and state");
		}
		encoder.encode(request);
		os.flush();

		return decoder.decode(request);
	}

	@Override
	public void sendCommands(OneWireRequest<?>... requests) throws IOException {
		readGarbage();
		for (OneWireRequest<?> request : requests) {
			switch (state) {
			case COMMAND:
				if (request instanceof DataRequest) {
					setState(State.DATA);
				}
				break;
			case DATA:
				if ((request instanceof CommandRequest) || (request instanceof CommunicationRequest)) {
					setState(State.COMMAND);
				}
				break;
			default:
				throw new RuntimeException("Can't hande request and state");
			}
			encoder.encode(request);
		}

		os.flush();

		for (OneWireRequest<?> request : requests) {
			decoder.decode(request);
		}
	}

	public void setSerialPort(SerialPortSocket serialPort) {
		this.serialPort = serialPort;
	}

	protected void readGarbage() throws IOException {
		if (is.available() > 0) {
			is.read(new byte[is.available()]);
		}
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
		os.write(Encoder.RESET_CMD);
		os.flush();
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
				new SingleBitRequest(Speed.STANDARD, DataToSend.WRITE_1_OR_READ_BIT, false));

	}

	@Override
	public void open() throws IOException {
		serialPort.openRaw(Baudrate.B9600, DataBits.DB_8, StopBits.SB_1, Parity.NONE, FlowControl.getFC_NONE());
		serialPort.setTimeouts(100, 1000, 1000);
		is = new BufferedInputStream(serialPort.getInputStream(), 64);
		os = new BufferedOutputStream(serialPort.getOutputStream(), 64);
		encoder = new Encoder(os);
		decoder = new Decoder(is);
		init();
	}

	@Override
	public void close() throws IOException {
		serialPort.close();
	}

	@Override
	public boolean isOpen() {
		return serialPort == null ? false : serialPort.isOpen();
	}

	@Override
	public void searchDevices(Consumer<Long> c) throws IOException {
		final OWSearchIterator searchIterator = new OWSearchIterator();
		final RawDataRequest searchCommandData = new RawDataRequest(16);
		final SearchCommand searchCommand = new SearchCommand();
		while (!searchIterator.isSearchFinished()) {
			sendCommand(ResetDeviceRequest.of(speedFromBaudrate));
			sendCommands(searchCommand.resetState(), 
					                           SearchAcceleratorCommand.of(SearchAccelerator.ON, speedFromBaudrate),
					searchCommandData.resetState(), 
					SearchAcceleratorCommand.of(SearchAccelerator.OFF, speedFromBaudrate));

			searchIterator.interpretSearch(searchCommandData);
			// check results
			if (searchIterator.getAddress() == 0xffffffffffffffffL) {
				//nothing found
				return; 
			}
			if (!OneWireContainer.isAsddressValid(searchIterator.getAddress())) {
				throw new RuntimeException("SearchError! invalid address: " + Long.toHexString(searchIterator.getAddress()));
			}
			c.accept(searchIterator.getAddress());
		}
	}

	@Override
	public void sendMatchRomRequest(long address) throws IOException {
		sendReset();
		final DataRequestWithDeviceCommand request = new DataRequestWithDeviceCommand(Encoder.MATCH_ROM_CMD, OneWireContainer.arrayOfLong(address));
		sendCommand(request);
		long result = OneWireContainer.addressOf(request.response);
		if (result != address) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void sendReset() throws IOException {
		sendCommand(ResetDeviceRequest.of(getSpeedFromBaudrate()));
	}

	@Override
	public byte[] sendRawDataRequest(byte[] data) throws IOException {
		final RawDataRequest request = new RawDataRequest(data);
		sendCommand(request);
		return request.response;
	}

	//DODO Use Duration??? and Infinite Reset???
	@Override
	public byte sendByteWithPower(byte b, StrongPullupDuration strongPullupDuration, Speed speed) throws IOException {
		CommandRequest<?>[] requests = new CommandRequest[9];
		requests[0] = ConfigurationWriteRequest.of(strongPullupDuration);
		for (int i = 0; i < 8; i++) {
			final SingleBitRequest sbr = new SingleBitRequest();
			sbr.dataToSend = (b & 0x01) == 0x01 ? DataToSend.WRITE_1_OR_READ_BIT : DataToSend.WRITE_0_BIT;
					b >>= 1;
			sbr.speed = speed;
			sbr.armPowerDelivery = (i < 7) ? false : true;
			requests[i +1] = sbr;
		}
		sendCommands(requests);
		byte result = 0;
		for (int i = 0; i < 8; i++) {
			final SingleBitResponse sbr = ((SingleBitRequest)requests[i +1]).response;
			switch (sbr.bitResult)	{
			case _O_READ_BACK:
				break;
			case _1_READ_BACK:
				result |= (byte) (0x01 << i);
				break;
				default:
					throw new RuntimeException();
			}
		}
		return result;
	}

	@Override
	public byte sendByte(byte b, Speed speed) throws IOException {
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
			final SingleBitResponse sbr = ((SingleBitRequest)requests[i]).response;
			switch (sbr.bitResult)	{
			case _O_READ_BACK:
				break;
			case _1_READ_BACK:
				result |= (byte) (0x01 << i);
				break;
				default:
					throw new RuntimeException();
			}
		}
		return result;
	}

	@Override
	public void sendTerminatePulse() throws IOException {
		CommandRequest<?>[] requests = new CommandRequest[3];
		requests[0] = new PulseTerminationRequest();
		requests[1] = PulseRequest.of(PulsePower.STRONG_PULLUP, PulseType.DISARM);
		requests[2] = new PulseTerminationRequest();
		sendCommands(requests);
		//TODO check response
	}

	@Override
	public void searchDevices(Consumer<OneWireContainer> c, boolean init) throws IOException {
		searchDevices((Long address)->{
			final OneWireDevice device = OneWireDevice.fromAdress(address, init);
			try {
				device.init(this);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} 
			c.accept(device);
		});
	}

	@Override
	public void sendSkipRomRequest() throws IOException {
		sendReset();
		final DataRequestWithDeviceCommand request = new DataRequestWithDeviceCommand(Encoder.SKIP_ROM_CMD, new byte[0]);
		sendCommand(request);
	}

	@Override
	public byte sendReadByteRequest() throws IOException {
		return sendCommand(new ReadBytesRequest(1))[0];
	}

}
