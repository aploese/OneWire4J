package de.ibapl.onewire.ng.request.data;

public class ReadBytesRequest extends RawDataRequest {

	public ReadBytesRequest(int size) {
		super(size, ONE_WIRE_READ_BYTE_FILLER);
	}

}
