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

/**
 * Utility class for CRC8 check sums.
 * 
 * @author Arne Plöse
 */
public class CRC8 {

    private final static byte[] CRC8_LOOKUP = new byte[256];

    static {

        for (int valueToLookup = 0; valueToLookup < 256; valueToLookup++) {
            int acc = valueToLookup;
            int crc8 = 0;

            for (int bitToCompute = 0; bitToCompute < 8; bitToCompute++) {
                if (((acc ^ crc8) & 0x01) == 0x01) {
                    crc8 = ((crc8 ^ 0x18) >> 1) | 0x80;
                } else {
                    crc8 = crc8 >> 1;
                }

                acc = acc >> 1;
            }

            CRC8_LOOKUP[valueToLookup] = (byte) crc8;
        }
    }

    /**
     * Checks a long where the MSB holds the CRC8
     * @param data
     * @return true if crc's match
     */
    public static boolean checkCrc8(long data) {
        byte crc8 = 0;
        for (int i = 0; i < 7; i++) {
            crc8 = CRC8_LOOKUP[(crc8 ^ ((int) (data >> (i * 8)))) & 0xFF];
        }
        return crc8 == data >> 56;
    }

    public static int computeCrc8(byte[] data) {
        return computeCrc8(data, 0, data.length, (byte)0);
    }
    
    public static int computeCrc8(byte[] data, int off, int len, byte seed) {

        byte crc8 = seed;

        for (int i = 0; i < len; i++) {
            crc8 = CRC8_LOOKUP[(crc8 ^ data[i + off]) & 0x0FF];
        }

        return (crc8 & 0x0FF);
    }

    private CRC8() {
    }

}
