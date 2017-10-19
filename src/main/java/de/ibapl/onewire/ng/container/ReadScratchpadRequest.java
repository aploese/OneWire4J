package de.ibapl.onewire.ng.container;

import de.ibapl.onewire.ng.request.data.DataRequestWithDeviceCommand;

public class ReadScratchpadRequest extends DataRequestWithDeviceCommand {

	@OneWireDataCommand
	public final static byte READ_SCRATCHPAD_CMD = (byte)0xbe;
	
	public ReadScratchpadRequest() {
		super(READ_SCRATCHPAD_CMD, 9, ONE_WIRE_READ_BYTE_FILLER);
	}

}
