package de.ibapl.onewire.ng.container;

import java.io.IOException;

import de.ibapl.onewire.ng.cli.OneWireAdapter;
import de.ibapl.onewire.ng.request.data.DataRequestWithDeviceCommand;

public abstract class OneWireDevice implements OneWireContainer {

	private final long address;

	protected OneWireDevice(long address) {
		this.address = address;
	}

	public static OneWireDevice fromAdress(long address, boolean init) {
		switch ((int) address & 0xff) {
		case 0x10:
			return new OneWireDevice10(address);
		case 0x28:
			return new OneWireDevice28(address);
		default:
			throw new RuntimeException("Cant handle One wire family: " + Integer.toHexString((int) address & 0xff));
		}
	}

	
	//TODO get a readTimeslot???? parasite power
	public void init(OneWireAdapter adapter) throws IOException {
	}
	

	@Override
	public long getAddress() {
		return address;
	}
	
	@Override
	public String getAddressAsString() {
		return String.format("%08x", address);
	}

}
