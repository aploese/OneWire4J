package de.ibapl.onewire.ng;

import java.io.IOException;

import de.ibapl.onewire.ng.cli.OneWireAdapter;
import de.ibapl.spsw.api.SerialPortSocket;

public class AdapterFactory {

		public OneWireAdapter open(SerialPortSocket serialPort) throws IOException {
			final DS2480BAdapter result = new DS2480BAdapter();
			result.setSerialPort(serialPort);
			result.open();
			return result;
		}
}
