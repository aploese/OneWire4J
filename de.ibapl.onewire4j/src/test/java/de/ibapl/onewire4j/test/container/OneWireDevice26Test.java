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
import de.ibapl.onewire4j.container.OneWireDevice26;
import de.ibapl.onewire4j.container.OneWireDevice26.StatusConfiguration;
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
public class OneWireDevice26Test {

    private static OneWireNetworks NETWORK;

    private OneWireAdapter adapter;
    private final static List<OneWireDevice26> containers = new LinkedList<>();

    @BeforeAll
    public static void setUpClass() throws Exception {
        File file = new File(OneWireDevice26Test.class.getResource("/junit-onewire4j-config.yaml").getFile());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        NETWORK = mapper.readValue(file, OneWireNetworks.class);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        ServiceLoader<SerialPortSocketFactory> spsFactory = ServiceLoader.load(SerialPortSocketFactory.class);
        SerialPortSocketFactory serialPortSocketFactory = spsFactory.iterator().next();
        System.out.println("serialPortSocketFactory " + serialPortSocketFactory.getClass().getName());
        final SerialPortSocket port = serialPortSocketFactory.open(NETWORK.networks.get(0).serialPort.name);
        LoggingSerialPortSocket lport = LoggingSerialPortSocket.wrapWithHexOutputStream(port,
                new FileOutputStream("owapi-ng.log"), false, TimeStampLogging.UTC);

        adapter = new AdapterFactory().open(lport, 1);
        System.err.print("Addresses:");
        adapter.searchDevices(SearchCommand.SEARCH_ROM, (OneWireContainer owc) -> {
            if (owc instanceof OneWireDevice26) {
                System.err.append(' ').append(owc.getAddressAsString());
                for (Device d : NETWORK.networks.get(0).serialPort.devices) {
                    if (owc.getAddressAsString().equals(d.address)) {
                        containers.add((OneWireDevice26) owc);
                    }
                }
            }
        });
        System.err.println();
    }

    @AfterEach
    public void tearDown() throws Exception {
        adapter.close();
    }

    public OneWireDevice26Test() throws IOException {
    }

    @Test
    public void testGetScratchpadPage0Data() throws Exception {
        System.out.println("testGetScratchpadPage0Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            OneWireDevice26.ScratchpadPage0Data data = instance.getScratchpadPage0(adapter, true, true);
            assertEquals(StatusConfiguration.IAD | StatusConfiguration.CA | StatusConfiguration.EE | StatusConfiguration.AD, data.getStatusConfiguration());
            assertNotEquals(0.0, data.getTemperature());
            assertNotEquals(0.0, data.getVoltage());
            assertEquals(0, data.getThreshold());
        }
    }

    @Test
    public void testHumidity_HIH4031() throws Exception {
        System.out.println("testGetScratchpadPage0Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            OneWireDevice26.ScratchpadPage0Data data = instance.getScratchpadPage0(adapter, false, false);
            final boolean old_AD = (data.getStatusConfiguration() & StatusConfiguration.AD) == StatusConfiguration.AD;
            data.setStatusConfiguration((byte) (data.getStatusConfiguration() | StatusConfiguration.AD));
            instance.setScratchpadPage0(adapter, data);
            data = instance.getScratchpadPage0(adapter, false, true);
            final double VDD = data.getVoltage();
            data.setStatusConfiguration((byte) (data.getStatusConfiguration() & ~StatusConfiguration.AD));
            instance.setScratchpadPage0(adapter, data);
            data = instance.getScratchpadPage0(adapter, true, true);
            final double VAD = data.getVoltage();
            if (old_AD) {
                data.setStatusConfiguration((byte) (data.getStatusConfiguration() | StatusConfiguration.AD));
                instance.setScratchpadPage0(adapter, data);
            }

            //Humidity
            final double T = data.getTemperature();

            final double humidity_uncompensated = ((VAD / VDD) - 0.16) / 0.0062;

            // temperature compensation
            final double temperature_compensation = 1.0546 - 0.00216 * T;
            System.err.println("Luftfeuchte: " + (humidity_uncompensated / temperature_compensation));

            final double rh = (161.29 * VAD / VDD - 25.8065) / (1.0546 - 0.00216 * T);

            System.err.println("Luftfeuchte RH: " + rh);

            final double rh_5V = (161.29 * VAD / 5.0 - 25.8065) / (1.0546 - 0.00216 * T);

            System.err.println("Luftfeuchte(5V) RH: " + rh_5V);

        }
    }

    @Test
    public void testGetScratchpadPage1Data() throws Exception {
        System.out.println("testGetScratchpadPage1Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            OneWireDevice26.ScratchpadPage1Data data = instance.getScratchpadPage1(adapter);
            assertNotEquals(0, data.getETM());
            assertNotEquals(0, data.getICA());
            assertNotEquals(0, data.getOffset());
            assertEquals(-4, data.getReserved());
        }
    }

    @Test
    public void testGetScratchpadPage2Data() throws Exception {
        System.out.println("testGetScratchpadPage2Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            OneWireDevice26.ScratchpadPage2Data data = instance.getScratchpadPage2(adapter);
            assertEquals(0, data.getDisconnect());
            assertEquals(0, data.getEndOfCharge());
        }
    }

    @Test
    public void testGetScratchpadPage3Data() throws Exception {
        System.out.println("testGetScratchpadPage3Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            fail();
        }
    }

    @Test
    public void testGetScratchpadPage4Data() throws Exception {
        System.out.println("testGetScratchpadPage4Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            fail();
        }
    }

    @Test
    public void testGetScratchpadPage5Data() throws Exception {
        System.out.println("testGetScratchpadPage5Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            fail();
        }
    }

    @Test
    public void testGetScratchpadPage6Data() throws Exception {
        System.out.println("testGetScratchpadPage6Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            fail();
        }
    }

    @Test
    public void testGetScratchpadPage7Data() throws Exception {
        System.out.println("testGetScratchpadPage7Data");
        Assumptions.assumeFalse(containers.isEmpty());
        for (OneWireDevice26 instance : containers) {
            OneWireDevice26.ScratchpadPage7Data data = instance.getScratchpadPage7(adapter);
            assertEquals(0, data.getCCA());
            assertEquals(0, data.getDCA());
        }
    }

}
