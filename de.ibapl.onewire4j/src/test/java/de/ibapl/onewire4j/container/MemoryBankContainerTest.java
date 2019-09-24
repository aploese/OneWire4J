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
package de.ibapl.onewire4j.container;

import de.ibapl.onewire4j.AdapterFactory;
import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author aploese
 */
public class MemoryBankContainerTest {
    
    public MemoryBankContainerTest() {
    }

    private OneWireAdapter adapter;
    private static String SERIAL_PORT_NAME = "/dev/ttyUSB0";
    private final static long TESTABLE_CONTAINERS[] = new long[]{0x710000190909132dL};
    private final static List<MemoryBankContainer> containers = new LinkedList<>();
    
    @BeforeAll
    public static void setUpClass() throws Exception {
    }
    
    @AfterAll
    public static void tearDownClass() throws Exception {
    }
    
    @BeforeEach
    public void setUp() throws Exception {
            ServiceLoader<SerialPortSocketFactory> spsFactory = ServiceLoader.load(SerialPortSocketFactory.class);
            SerialPortSocketFactory serialPortSocketFactory = spsFactory.iterator().next();
            System.out.println("serialPortSocketFactory " + serialPortSocketFactory.getClass().getName());
            final SerialPortSocket port = serialPortSocketFactory.createSerialPortSocket(SERIAL_PORT_NAME);
            LoggingSerialPortSocket lport = LoggingSerialPortSocket.wrapWithHexOutputStream(port,
                    new FileOutputStream("owapi-ng.log"), false, TimeStampLogging.UTC);

            adapter = new AdapterFactory().open(lport, 1);
                System.err.print("Addresses:");
                adapter.searchDevices((OneWireContainer owc) -> {
                    System.err.append(' ').append(owc.getAddressAsString());
                    for (long l : TESTABLE_CONTAINERS) {
                        if (l == owc.getAddress()) {
                            containers.add((MemoryBankContainer)owc);
                        }
                    }
                });
                System.err.println();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        adapter.close();
    }

    /**
     * Test of writeToMemory method, of class MemoryBankContainer.
     */
    @org.junit.jupiter.api.Test
    public void testWriteToMemory() throws Exception {
        System.out.println("writeMemory");
        byte data[] = new byte[] {0x11, 0x48, 0x02, 0x03, 0x04, 0x05, 0x60, (byte)0xf0};
        for (MemoryBankContainer mbc: containers) {
            Assertions.assertTrue(mbc.writeToMemory(adapter, 0x0008, data, 0, data.length), "Unsuccessful write to memory");
            Assertions.assertArrayEquals(data, mbc.readMemory(adapter, 0x0008, 8));
        }
    }

    
    /**
     * Test of writeToMemory method, of class MemoryBankContainer.
     */
    @org.junit.jupiter.api.Test
    public void testReadMemory() throws Exception {
        System.out.println("readMemory");
        for (MemoryBankContainer mbc: containers) {
            byte[] data = mbc.readMemory(adapter, 0x00, 8);
            Assertions.assertEquals(8, data.length);
        }
    }

    
}
