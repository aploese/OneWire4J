package de.ibapl.onewire.ng.container;


public class OneWireDevice10 extends OneWireDevice implements TemperatureContainer {

	public OneWireDevice10(long address) {
		super(address);
	}

	public double getTemperature(ReadScratchpadRequest request) {

		// on some parts, namely the 18S20, you can get invalid readings.
		// basically, the detection is that all the upper 8 bits should
		// be the same by sign extension. the error condition (DS18S20
		// returns 185.0+) violated that condition
		if (((request.response[1] & 0x0ff) != 0x00) && ((request.response[1] & 0x0ff) != 0x0FF))
			throw new RuntimeException("Invalid temperature data!");

		int temp = (request.response[0] & 0x0ff) | (request.response[1] << 8);
		temp >>= 1;
		return (double)temp - 0.25 + ((double) request.response[7] - (double) request.response[6]) / (double) request.response[7];
	}

}
