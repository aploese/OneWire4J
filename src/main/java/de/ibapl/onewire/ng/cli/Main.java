/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibapl.onewire.ng.cli;

import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.rmi.server.LogStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;

import de.ibapl.onewire.adapter.OneWireIOException;
import de.ibapl.onewire.ng.AdapterFactory;
import de.ibapl.onewire.ng.container.ENotProperlyConvertedException;
import de.ibapl.onewire.ng.container.OneWireContainer;
import de.ibapl.onewire.ng.container.ReadScratchpadRequest;
import de.ibapl.onewire.ng.container.TemperatureContainer;
import de.ibapl.onewire.ng.request.communication.PulseRequest;
import de.ibapl.onewire.ng.request.configuration.StrongPullupDuration;
import de.ibapl.onewire.ng.request.data.RawDataRequest;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import de.ibapl.spsw.provider.SerialPortSocketFactoryImpl;

/**
 *
 * @author aploese
 */
public class Main {

	public static double getTemperature28(ReadScratchpadRequest rawDataRequest) {
		double theTemperature = (double) 0.0;
		// inttemperature is automatically sign extended here.
		final int inttemperature = (rawDataRequest.response[0] & 0xFF) | (rawDataRequest.response[1] << 8); // this converts 2 bytes into
																						// integer
		theTemperature = (double) inttemperature / 16.0; // converts integer to a double

		return (theTemperature);
	}

	public static double getTemperature10(ReadScratchpadRequest rawDataRequest) throws OneWireIOException {

		// on some parts, namely the 18S20, you can get invalid readings.
		// basically, the detection is that all the upper 8 bits should
		// be the same by sign extension. the error condition (DS18S20
		// returns 185.0+) violated that condition
		if (((rawDataRequest.response[1] & 0x0ff) != 0x00) && ((rawDataRequest.response[1] & 0x0ff) != 0x0FF))
			throw new OneWireIOException("Invalid temperature data!");

		int temp = (rawDataRequest.response[0] & 0x0ff) | (rawDataRequest.response[1] << 8);
		temp >>= 1;
		return (double)temp - 0.25 + ((double) rawDataRequest.response[7] - (double) rawDataRequest.response[6]) / (double) rawDataRequest.response[7];
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		FileOutputStream log = new FileOutputStream("/tmp/owapi-ng.log");
		final SerialPortSocket port = SerialPortSocketFactoryImpl.singleton().createSerialPortSocket(args[0]);
		LoggingSerialPortSocket lport = LoggingSerialPortSocket.wrapWithHexOutputStream(port,
				new FileOutputStream("/tmp/owapi-ng.csv"), false, TimeStampLogging.UTF);
		
		try (OneWireAdapter adapter = new AdapterFactory().open(lport)) {
			final boolean parasitePowerNeeded = TemperatureContainer.isParasitePower(adapter);
			System.err.println("Some device uses parasite power: " + parasitePowerNeeded);
			final LinkedList<OneWireContainer> owcs = new LinkedList<>();
			System.err.print("Addresses:");
			adapter.searchDevices((OneWireContainer owc) -> {
				System.err.append(' ').append(owc.getAddressAsString());
				owcs.add(owc);
			}, true);
			System.err.println();


			
			String logString = "";
			while (true) {
				TemperatureContainer.sendDoConvertRequestToAll(adapter, parasitePowerNeeded);
				for (OneWireContainer owc : owcs) {
					if (owc instanceof TemperatureContainer) {
						final TemperatureContainer tc = (TemperatureContainer)owc;
						try {
							ReadScratchpadRequest request = new ReadScratchpadRequest();
							tc.readScratchpad(adapter, request);
							final double temp  =  tc.getTemperature(request);
							logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + temp + "°C\n";
							log.write(logString.getBytes());
						} catch (ENotProperlyConvertedException e) {
							logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + e.getValue() + "°C\t ERROR? \n";
							log.write(logString.getBytes());
							try {
								final double temp  =  tc.convertAndReadTemperature(adapter);
								logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + temp + "°C\n";
								log.write(logString.getBytes());
							} catch (ENotProperlyConvertedException e1) {
								logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + e.getValue() + "°C\t ERROR AGAIN ... MAYBE NOT \n";
								System.err.print(logString);
								log.write(logString.getBytes());
							}
						}
					}
				}
			}
		}
	}
}
