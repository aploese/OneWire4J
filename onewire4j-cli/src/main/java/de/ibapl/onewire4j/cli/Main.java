/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2019, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.ibapl.onewire4j.cli;

import java.io.FileOutputStream;
import java.time.Instant;
import java.util.LinkedList;
import java.util.ServiceLoader;

import de.ibapl.onewire4j.AdapterFactory;
import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.ENotProperlyConvertedException;
import de.ibapl.onewire4j.container.MemoryBankContainer;
import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.TemperatureContainer;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;

/**
 *
 * @author Arne Plöse
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        try (FileOutputStream log = new FileOutputStream("/tmp/owapi-ng.csv")) {
            ServiceLoader<SerialPortSocketFactory> spsFactory = ServiceLoader.load(SerialPortSocketFactory.class);
            SerialPortSocketFactory serialPortSocketFactory = spsFactory.iterator().next();
            System.out.println("serialPortSocketFactory " + serialPortSocketFactory.getClass().getName());
            if (args.length == 0) {
                throw new IllegalArgumentException("Portname is missing");
            }
            final SerialPortSocket port = serialPortSocketFactory.createSerialPortSocket(args[0]);
            LoggingSerialPortSocket lport = LoggingSerialPortSocket.wrapWithHexOutputStream(port,
                    new FileOutputStream("/tmp/owapi-ng.log"), false, TimeStampLogging.UTC);

            try (OneWireAdapter adapter = new AdapterFactory().open(lport, 1)) {
                final boolean parasitePowerNeeded = TemperatureContainer.isParasitePower(adapter);
                System.err.println("Some device uses parasite power: " + parasitePowerNeeded);

                final LinkedList<OneWireContainer> owcs = new LinkedList<>();
                System.err.print("Addresses:");
                adapter.searchDevices((OneWireContainer owc) -> {
                    System.err.append(' ').append(owc.getAddressAsString());
                    owcs.add(owc);
                });
                System.err.println();

                String logString = "";
                while (true) {
                    TemperatureContainer.sendDoConvertRequestToAll(adapter, parasitePowerNeeded);
                    for (OneWireContainer owc : owcs) {
                        if (owc instanceof TemperatureContainer) {
                            final TemperatureContainer tc = (TemperatureContainer) owc;
                            try {
                                TemperatureContainer.ReadScratchpadRequest request = new TemperatureContainer.ReadScratchpadRequest();
                                tc.readScratchpad(adapter, request);
                                final double temp = tc.getTemperature(request);
                                logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + temp + "°C\n";
                                log.write(logString.getBytes());
                            } catch (ENotProperlyConvertedException e) {
                                logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + e.getValue()
                                        + "°C\t ERROR? \n";
                                log.write(logString.getBytes());
                                try {
                                    final double temp = tc.convertAndReadTemperature(adapter);
                                    logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + temp + "°C\n";
                                    log.write(logString.getBytes());
                                } catch (ENotProperlyConvertedException e1) {
                                    logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + e.getValue()
                                            + "°C\t ERROR AGAIN ... MAYBE NOT \n";
                                    System.err.print(logString);
                                    log.write(logString.getBytes());
                                }
                            }
                        } else if (owc instanceof MemoryBankContainer) {
                            final MemoryBankContainer mc = (MemoryBankContainer) owc;
                            byte[] v = mc.readMemory(adapter, 0, 8);
                            logString = Instant.now() + "\t" + owc.getAddressAsString() + "\t" + bytes2HexString(v) + "\n";
                            System.err.print(logString);
                            log.write(logString.getBytes());
                        }
                    }
                }
            }
        }
    }

    private static String bytes2HexString(byte[] value) {
        if (value == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("0x%02x, ", b));
        }
        return sb.substring(0, sb.length() -2);
    }
}
