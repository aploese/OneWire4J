package de.ibapl.onewire.ng.container;

import java.io.IOException;

import de.ibapl.onewire.ng.cli.OneWireAdapter;

public class OneWireDevice28 extends OneWireDevice implements TemperatureContainer {

	public OneWireDevice28(long address) {
		super(address);
	}

	@Override
	public double getTemperature(ReadScratchpadRequest request) throws ENotProperlyConvertedException {
		final int intTemperature = (request.response[0] & 0xFF) | (request.response[1] << 8); // this converts 2 bytes into
		if (intTemperature == 0x5005) {
			// Sometimes 85°C appears maybe a hidden conversation error??
			throw new ENotProperlyConvertedException(85);
		}
		final double result = (double) intTemperature / 16.0; // converts integer to a double
		return (result);
	}

}
