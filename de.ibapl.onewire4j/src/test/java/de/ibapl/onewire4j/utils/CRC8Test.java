/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 - 2018 Arne Plöse
 * %%
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2009, 2017, Arne Plöse and individual contributors as indicated
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
 * #L%
 */
package de.ibapl.onewire4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Arne Plöse
 */
public class CRC8Test {
    
    public CRC8Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of compute method, of class CRC8.
     */
    @Test
    public void testCompute_byteArr() {
        System.out.println("compute");
//        byte[] dataToCRC = new byte[] {(byte)0x55, (byte)0x00, (byte)0x08, (byte)0x03, (byte)0x35, (byte)0xf5, (byte)0x21, (byte)0x10};
        byte[] dataToCRC = new byte[] {(byte)0x10, (byte)0x21, (byte)0xf5, (byte)0x35, (byte)0x03, (byte)0x08, (byte)0x00, (byte)0x55 };
        int expResult = dataToCRC[7];
        int result = CRC8.computeCrc8(dataToCRC, 0, 7, (byte)0);
        assertEquals(expResult, result);
        assertTrue(CRC8.checkCrc8(0x5500080335f52110L));
    }

}
