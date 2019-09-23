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
package de.ibapl.onewire4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Arne Plöse
 */
public class CRC8Test {

    public static byte computeCRC8(byte value, byte seed) {
        byte acc = value;
        //TODO byte
        int crc8 = seed & 0xff;

        for (int bitToCompute = 0; bitToCompute < 8; bitToCompute++) {
            if (((acc ^ crc8) & 0x01) == 0x01) {
                crc8 = ((crc8 ^ 0x18) >> 1) | 0x80;
            } else {
                crc8 >>= 1;
            }

            acc >>= 1;
        }
        return (byte) crc8;

    }

    public CRC8Test() {
    }

    /**
     * Test of compute method, of class CRC8.
     */
    @Test
    public void testCrc8Array() {
        System.out.println("compute");
//        byte[] dataToCRC = new byte[] {(byte)0x55, (byte)0x00, (byte)0x08, (byte)0x03, (byte)0x35, (byte)0xf5, (byte)0x21, (byte)0x10};
        byte[] dataToCRC = new byte[]{(byte) 0x10, (byte) 0x21, (byte) 0xf5, (byte) 0x35, (byte) 0x03, (byte) 0x08, (byte) 0x00, (byte) 0x55};
        int expResult = dataToCRC[7];
        int result = CRC8.crc8(dataToCRC, 0, 7, (byte) 0);
        assertEquals(expResult, result);
    }

    @Test
    public void testOneComplement() throws Exception {
        for (int value = 0; value <= 0xff; value++) {
            assertEquals(CRC8.CRC8_OF_SEEDS_ONE_COMPLEMENT, CRC8.crc8((byte) ~value, (byte) value));
            assertEquals(CRC8.CRC8_OF_SEEDS_ONE_COMPLEMENT, computeCRC8((byte) ~value, (byte) value));
        }
    }

    @Test
    public void testSameInputAsSeed() throws Exception {
        for (int value = 0; value <= 0xff; value++) {
            assertEquals(0, CRC8.crc8((byte) value, (byte) value));
            assertEquals(0, computeCRC8((byte) value, (byte) value));
        }
    }

    @Test
    public void testAddress() throws Exception {
        assertTrue(CRC8.isAddressValid(0x710000190909132dL));
    }

    @Test
    public void testCrc8() {
        for (int seed = 0; seed < 0xff; seed++) {
            for (int value = 0; value < 0xff; value++) {
                assertEquals(computeCRC8((byte) value, (byte) seed), CRC8.crc8((byte) value, (byte) seed));
            }
        }
    }
}
