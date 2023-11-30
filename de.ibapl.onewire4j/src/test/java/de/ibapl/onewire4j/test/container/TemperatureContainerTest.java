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
package de.ibapl.onewire4j.test.container;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.ibapl.onewire4j.AdapterFactory;
import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.TemperatureContainer;
import de.ibapl.onewire4j.request.data.SearchCommand;
import de.ibapl.onewire4j.test.network.Device;
import de.ibapl.onewire4j.test.network.OneWireNetworks;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author aploese
 */
public class TemperatureContainerTest {

    private static OneWireNetworks NETWORK;

    private OneWireAdapter adapter;
    private final static List<TemperatureContainer> containers = new LinkedList<>();

    @BeforeAll
    public static void setUpClass() throws Exception {
        URL resource = OneWireDevice26Test.class.getResource("/junit-onewire4j-config.yaml");
        if (resource == null) {
            NETWORK = null;
        } else {
            File file = new File(resource.getFile());
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            NETWORK = mapper.readValue(file, OneWireNetworks.class);
        }
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        Assumptions.assumeTrue(NETWORK != null); //stop here if ther is no NETWORK i.e. no config file
        ServiceLoader<SerialPortSocketFactory> spsFactory = ServiceLoader.load(SerialPortSocketFactory.class);
        SerialPortSocketFactory serialPortSocketFactory = spsFactory.iterator().next();
        System.out.println("serialPortSocketFactory " + serialPortSocketFactory.getClass().getName());
        final SerialPortSocket port = serialPortSocketFactory.open(NETWORK.networks.get(0).serialPort.name);
        LoggingSerialPortSocket lport = LoggingSerialPortSocket.wrapWithHexOutputStream(port,
                new FileOutputStream("owapi-ng.log"), false, TimeStampLogging.UTC);

        adapter = new AdapterFactory().open(lport, 1);
        System.err.print("Addresses:");
        adapter.searchDevices(SearchCommand.SEARCH_ROM, (OneWireContainer owc) -> {
            if (owc instanceof TemperatureContainer) {
                System.err.append(' ').append(owc.getAddressAsString());
                for (Device d : NETWORK.networks.get(0).serialPort.devices) {
                    if (owc.getAddressAsString().equals(d.address)) {
                        containers.add((TemperatureContainer) owc);
                    }
                }
            }
        });
        System.err.println();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (adapter != null) {
            adapter.close();
        }
    }

    public TemperatureContainerTest() throws IOException {
    }

    @Test
    public void testGetTemperature() throws Exception {
        System.out.println("getTemperature");
        Assumptions.assumeFalse(containers.isEmpty());
        for (TemperatureContainer instance : containers) {
            TemperatureContainer.ReadScratchpadRequest request = new TemperatureContainer.ReadScratchpadRequest();
            final boolean isParasitePowerNeeded = instance.isUsingParasitePower(adapter);
            instance.sendDoConvertTRequest(adapter, isParasitePowerNeeded);
            instance.readScratchpad(adapter, request);
            double resultTemperature = instance.getTemperature(request);
            resultTemperature = instance.convertAndReadTemperature(adapter, isParasitePowerNeeded);
        }
    }

    @Test
    public void testIsUsingParasitePower() throws Exception {
        System.out.println("testSetGetAlarmsToEEPROM");
        Assumptions.assumeFalse(containers.isEmpty());
        for (TemperatureContainer instance : containers) {
            boolean isParasitePowerNeeded = instance.isUsingParasitePower(adapter);
            assertFalse(isParasitePowerNeeded);
        }

    }

}
