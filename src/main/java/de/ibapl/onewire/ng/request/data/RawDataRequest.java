package de.ibapl.onewire.ng.request.data;

import java.util.Arrays;

public class RawDataRequest extends DataRequest<byte[]> {
	
	public RawDataRequest(int size, int filler) {
		this(size);
		Arrays.fill(requestData, (byte) filler);
	}

	/**
	 * 
	 * @param size the size of the buffer
	 */
	public RawDataRequest(int size) {
		this(new byte[size]);
	}

	public RawDataRequest(byte[] requestData, byte[] responseArray) {
		this.requestData = requestData;
		this.response = responseArray;
	}

	public RawDataRequest(byte[] requestData) {
		this(requestData, new byte[requestData.length]);
	}

	public final byte[] requestData;

}
