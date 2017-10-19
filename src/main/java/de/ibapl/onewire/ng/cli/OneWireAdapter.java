package de.ibapl.onewire.ng.cli;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

import de.ibapl.onewire.ng.container.OneWireContainer;
import de.ibapl.onewire.ng.request.OneWireRequest;
import de.ibapl.onewire.ng.request.Speed;
import de.ibapl.onewire.ng.request.configuration.StrongPullupDuration;
import de.ibapl.spsw.api.SerialPortSocket;

public interface OneWireAdapter extends Closeable {
	
	void open() throws IOException;
	
	void setSerialPort(SerialPortSocket serialPort);
	
	boolean isOpen();

	void searchDevices(Consumer<Long> c) throws IOException;
	
	void searchDevices(Consumer<OneWireContainer> d, boolean init) throws IOException;

	Speed getSpeedFromBaudrate();
	
	void sendMatchRomRequest(long address) throws IOException ;

	void sendReset() throws IOException;
	
	byte[] sendRawDataRequest(byte[] data) throws IOException;
	
	byte sendByteWithPower(byte b, StrongPullupDuration strongPullupDuration, Speed speed) throws IOException;

	byte sendByte(byte b, Speed speed) throws IOException;

	<R> R sendCommand(OneWireRequest<R> request) throws IOException;

	void sendCommands(OneWireRequest<?>[] requests) throws IOException;
	
	void sendTerminatePulse() throws IOException;

	void sendSkipRomRequest() throws IOException;

	byte sendReadByteRequest() throws IOException;
	
}
