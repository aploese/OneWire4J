/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibapl.onewire;

import de.ibapl.onewire.adapter.DSPortAdapter;
import de.ibapl.onewire.adapter.OneWireIOException;
import de.ibapl.onewire.container.OneWireContainer;
import de.ibapl.onewire.container.OneWireSensor;
import de.ibapl.onewire.container.SwitchContainer;
import de.ibapl.onewire.container.TemperatureContainer;
import de.ibapl.spsw.provider.SerialPortSocketFactoryImpl;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author aploese
 */
public class Cli {

	public static void main(String[] args) throws Exception {
		DSPortAdapter adapter;
		adapter = OneWireAccessProvider.getAdapter(SerialPortSocketFactoryImpl.singleton(), "/dev/ttyUSB0",
				"/tmp/onewire.txt");
		adapter.setSearchAllDevices();
		adapter.targetAllFamilies();
		adapter.setSpeed(OneWireSpeed.REGULAR);
		initializeImpl(adapter);
	}

	private static void initializeImpl(DSPortAdapter adapter) throws OneWireException, IOException {
		LinkedList<TemperatureContainer> containers = new LinkedList<>();

		// Start a search for all devices.
		boolean searchResult = adapter.findFirstDevice();
		while (searchResult) {
			if (adapter.getDeviceContainer() instanceof TemperatureContainer) {
				containers.add((TemperatureContainer) adapter.getDeviceContainer());
			}
			searchResult = adapter.findNextDevice();
		}
		while (true) {
			for (TemperatureContainer owc : containers) {
				byte[] tcState = new byte[8];
				owc.doTemperatureConvert(tcState);
				tcState = owc.readDevice();
				System.err.println(Instant.now() + "\t" + Long.toHexString(owc.getAddressAsLong()) + "\t"
						+ owc.getTemperature(tcState));
			}
		}
	}
}
