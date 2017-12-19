package de.ibapl.onewire4j.container;

/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 Arne Plöse
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
public class OneWireContainerTest {
    
    public OneWireContainerTest() {
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
     * Test of isAsddressValid method, of class OneWireContainer.
     */
    @Test
    public void testIsAsddressValid() {
        System.out.println("isAsddressValid");
        assertTrue(OneWireContainer.isAsddressValid(0x5500080335f52110L));
    }

    /**
     * Test of addressToString method, of class OneWireContainer.
     */
    @Test
    public void testAddressToString() {
        System.out.println("addressToString");
        String expResult = "5500080335f52110";
        String result = OneWireContainer.addressToString(0x5500080335f52110L);
        assertEquals(expResult, result);
    }

    /**
     * Test of arrayOfLong method, of class OneWireContainer.
     */
    @Test
    public void testArrayOfLong() {
        System.out.println("arrayOfLong");
        long address = 0L;
        byte[] expResult = new byte[] {(byte)0x10, (byte)0x21, (byte)0xf5, (byte)0x35, (byte)0x03, (byte)0x08, (byte)0x00, (byte)0x55 };
        byte[] result = OneWireContainer.arrayOfLong(0x5500080335f52110L);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of addressOf method, of class OneWireContainer.
     */
    @Test
    public void testAddressOf() {
        System.out.println("addressOf");
        byte[] address = new byte[] {(byte)0x10, (byte)0x21, (byte)0xf5, (byte)0x35, (byte)0x03, (byte)0x08, (byte)0x00, (byte)0x55 };
        long expResult = 0x5500080335f52110L;
        long result = OneWireContainer.addressOf(address);
        assertEquals(expResult, result);
    }
    
}
