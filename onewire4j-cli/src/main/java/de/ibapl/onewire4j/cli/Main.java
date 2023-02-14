/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2021, Arne Plöse and individual contributors as indicated
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

import de.ibapl.onewire4j.AdapterFactory;
import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.MemoryBankContainer;
import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.OneWireDevice26;
import de.ibapl.onewire4j.container.TemperatureContainer;
import de.ibapl.onewire4j.devices.DS2438WithHIH4031;
import de.ibapl.onewire4j.request.data.SearchCommand;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.ServiceLoader;

/**
 *
 * @author Arne Plöse
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        try ( FileOutputStream log = new FileOutputStream("/tmp/owapi-ng.csv")) {
            ServiceLoader<SerialPortSocketFactory> spsFactory = ServiceLoader.load(SerialPortSocketFactory.class);
            SerialPortSocketFactory serialPortSocketFactory = spsFactory.iterator().next();
            System.out.println("serialPortSocketFactory " + serialPortSocketFactory.getClass().getName());
            if (args.length == 0) {
                throw new IllegalArgumentException("Portname is missing");
            }
            final SerialPortSocket port = serialPortSocketFactory.open(args[0]);
            LoggingSerialPortSocket lport = LoggingSerialPortSocket.wrapWithHexOutputStream(port,
                    new FileOutputStream("/tmp/owapi-ng.log"), false, TimeStampLogging.UTC);

            try ( OneWireAdapter adapter = new AdapterFactory().open(lport, 1)) {
                final boolean parasitePowerNeeded = TemperatureContainer.isAnyTempDeviceUsingParasitePower(adapter);
                System.err.println("Some device uses parasite power: " + parasitePowerNeeded);

                final LinkedList<OneWireContainer> owcs = new LinkedList<>();
                /*
                System.err.print("Addresses:");
                adapter.searchDevices((OneWireContainer owc) -> {
                    System.err.append(' ').append(owc.getAddressAsString());
                    owcs.add(owc);
                });
                System.err.println();
                 */
                boolean doLoop = false;
                do {
                    try {
                        adapter.searchDevices(SearchCommand.SEARCH_ROM, (OneWireContainer owc) -> {
                            System.err.append(' ').append(owc.getAddressAsString());
                        });
                        System.err.println();
                        Thread.sleep(500);
                    } catch (Exception e) {
                        Thread.sleep(500);

                    }
                } while (doLoop);

                log.write("Timestamp".getBytes());
                adapter.searchDevices(SearchCommand.SEARCH_ROM, (OneWireContainer owc) -> {
                    owcs.add(owc);
                    try {
                        log.write((byte) '\t');
                        log.write(owc.getAddressAsString().getBytes());
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                });
                while (true) {
                    TemperatureContainer.sendDoConvertRequestToAll(adapter, parasitePowerNeeded);
                    log.write((byte) '\n');
                    log.write(Instant.now().toString().getBytes());
                    for (OneWireContainer owc : owcs) {
                        if (owc instanceof TemperatureContainer) {
                            final TemperatureContainer tc = (TemperatureContainer) owc;
                            log.write((byte) '\t');
                            TemperatureContainer.ReadScratchpadRequest request = new TemperatureContainer.ReadScratchpadRequest();
                            tc.readScratchpad(adapter, request);
                            if (tc.isTemperaturePowerOnResetValue(request)) {
                                final double temp = tc.convertAndReadTemperature(adapter, parasitePowerNeeded);
                                log.write(String.valueOf(temp).getBytes());
                            }
                            final double temp = tc.getTemperature(request);
                            log.write(String.valueOf(temp).getBytes());
                        } else if (owc instanceof OneWireDevice26) {
                            final OneWireDevice26 dev26 = (OneWireDevice26) owc;
                            final DS2438WithHIH4031 humidity = new DS2438WithHIH4031((OneWireDevice26) owc);
                            final double VAD;
                            final double VDD;
                            final double temp;

                            OneWireDevice26.ScratchpadPage0Data page0 = dev26.getScratchpadPage0(adapter, true, true);
                            temp = page0.getTemperature();
                            if (page0.isVDD()) {
                                VDD = page0.getVoltage();
                                page0.setVDD(false);
                                dev26.setScratchpadPage0(adapter, page0);
                                page0 = dev26.getScratchpadPage0(adapter, false, true);
                                VAD = page0.getVoltage();
                            } else {
                                VAD = page0.getVoltage();
                                page0.setVDD(true);
                                dev26.setScratchpadPage0(adapter, page0);
                                page0 = dev26.getScratchpadPage0(adapter, false, true);
                                VDD = page0.getVoltage();

                            }

                            log.write((byte) '\t');
                            log.write(String.valueOf(humidity.getHIH4031Humidity(adapter)).getBytes());

                            log.write((byte) '\t');
                            log.write(String.valueOf(temp).getBytes());

                            log.write((byte) '\t');
                            log.write(String.valueOf(VDD).getBytes());

                            log.write((byte) '\t');
                            log.write(String.valueOf(VAD).getBytes());

                        } else if (owc instanceof MemoryBankContainer) {
                            final MemoryBankContainer mc = (MemoryBankContainer) owc;
                            byte[] v = mc.readMemory(adapter, 0, 8);
                        }
                    }
                    Thread.sleep(60_000);
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
        return sb.substring(0, sb.length() - 2);
    }
}
