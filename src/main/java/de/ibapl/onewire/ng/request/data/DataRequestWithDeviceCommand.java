package de.ibapl.onewire.ng.request.data;

import java.util.Arrays;

public class DataRequestWithDeviceCommand extends DataRequest<byte[]> {
	
	/**
	 * 
	 * @param command the command is send first followed by the data
	 * @param size the remaining size for the data
	 * @param fill the byte with to fill the data section
	 */
	public DataRequestWithDeviceCommand(byte command, int size, byte filler) {
		this((byte)command, new byte[size], new byte[size]);
		Arrays.fill(requestData, filler);
	}

	public DataRequestWithDeviceCommand(byte command, byte[] requestData) {
		this(command, requestData, new byte[requestData.length]);
	}

	public DataRequestWithDeviceCommand(byte command, byte[] requestData, byte[] responseArray) {
		this.requestData = requestData;
		this.response = responseArray;
		this.command = (byte)command;
	}
	
	public final byte[] requestData;
	public final byte command;
}
