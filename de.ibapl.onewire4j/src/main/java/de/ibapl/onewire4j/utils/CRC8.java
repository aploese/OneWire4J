/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2023, Arne Plöse and individual contributors as indicated
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

/**
 * Utility class for CRC8 check sums.
 *
 * See
 * <a href="https://www.maximintegrated.com/en/design/technical-documents/app-notes/2/27.html">Application
 * Note 27</a>
 *
 * @author Arne Plöse
 */
public class CRC8 {

    public final static byte CRC8_OF_SEEDS_ONE_COMPLEMENT = 0x35;

    private final static byte[] CRC8_LOOKUP = new byte[]{0x00, 0x5e, (byte) 0xbc,
        (byte) 0xe2, 0x61, 0x3f, (byte) 0xdd, (byte) 0x83, (byte) 0xc2,
        (byte) 0x9c, 0x7e, 0x20, (byte) 0xa3, (byte) 0xfd, 0x1f, 0x41,
        (byte) 0x9d, (byte) 0xc3, 0x21, 0x7f, (byte) 0xfc, (byte) 0xa2, 0x40,
        0x1e, 0x5f, 0x01, (byte) 0xe3, (byte) 0xbd, 0x3e, 0x60, (byte) 0x82,
        (byte) 0xdc, 0x23, 0x7d, (byte) 0x9f, (byte) 0xc1, 0x42, 0x1c,
        (byte) 0xfe, (byte) 0xa0, (byte) 0xe1, (byte) 0xbf, 0x5d, 0x03,
        (byte) 0x80, (byte) 0xde, 0x3c, 0x62, (byte) 0xbe, (byte) 0xe0, 0x02,
        0x5c, (byte) 0xdf, (byte) 0x81, 0x63, 0x3d, 0x7c, 0x22, (byte) 0xc0,
        (byte) 0x9e, 0x1d, 0x43, (byte) 0xa1, (byte) 0xff, 0x46, 0x18,
        (byte) 0xfa, (byte) 0xa4, 0x27, 0x79, (byte) 0x9b, (byte) 0xc5,
        (byte) 0x84, (byte) 0xda, 0x38, 0x66, (byte) 0xe5, (byte) 0xbb, 0x59,
        0x07, (byte) 0xdb, (byte) 0x85, 0x67, 0x39, (byte) 0xba, (byte) 0xe4,
        0x06, 0x58, 0x19, 0x47, (byte) 0xa5, (byte) 0xfb, 0x78, 0x26,
        (byte) 0xc4, (byte) 0x9a, 0x65, 0x3b, (byte) 0xd9, (byte) 0x87, 0x04,
        0x5a, (byte) 0xb8, (byte) 0xe6, (byte) 0xa7, (byte) 0xf9, 0x1b, 0x45,
        (byte) 0xc6, (byte) 0x98, 0x7a, 0x24, (byte) 0xf8, (byte) 0xa6, 0x44,
        0x1a, (byte) 0x99, (byte) 0xc7, 0x25, 0x7b, 0x3a, 0x64, (byte) 0x86,
        (byte) 0xd8, 0x5b, 0x05, (byte) 0xe7, (byte) 0xb9, (byte) 0x8c,
        (byte) 0xd2, 0x30, 0x6e, (byte) 0xed, (byte) 0xb3, 0x51, 0x0f, 0x4e,
        0x10, (byte) 0xf2, (byte) 0xac, 0x2f, 0x71, (byte) 0x93, (byte) 0xcd,
        0x11, 0x4f, (byte) 0xad, (byte) 0xf3, 0x70, 0x2e, (byte) 0xcc,
        (byte) 0x92, (byte) 0xd3, (byte) 0x8d, 0x6f, 0x31, (byte) 0xb2,
        (byte) 0xec, 0x0e, 0x50, (byte) 0xaf, (byte) 0xf1, 0x13, 0x4d,
        (byte) 0xce, (byte) 0x90, 0x72, 0x2c, 0x6d, 0x33, (byte) 0xd1,
        (byte) 0x8f, 0x0c, 0x52, (byte) 0xb0, (byte) 0xee, 0x32, 0x6c,
        (byte) 0x8e, (byte) 0xd0, 0x53, 0x0d, (byte) 0xef, (byte) 0xb1,
        (byte) 0xf0, (byte) 0xae, 0x4c, 0x12, (byte) 0x91, (byte) 0xcf, 0x2d,
        0x73, (byte) 0xca, (byte) 0x94, 0x76, 0x28, (byte) 0xab, (byte) 0xf5,
        0x17, 0x49, 0x08, 0x56, (byte) 0xb4, (byte) 0xea, 0x69, 0x37,
        (byte) 0xd5, (byte) 0x8b, 0x57, 0x09, (byte) 0xeb, (byte) 0xb5, 0x36,
        0x68, (byte) 0x8a, (byte) 0xd4, (byte) 0x95, (byte) 0xcb, 0x29, 0x77,
        (byte) 0xf4, (byte) 0xaa, 0x48, 0x16, (byte) 0xe9, (byte) 0xb7, 0x55,
        0x0b, (byte) 0x88, (byte) 0xd6, 0x34, 0x6a, 0x2b, 0x75, (byte) 0x97,
        (byte) 0xc9, 0x4a, 0x14, (byte) 0xf6, (byte) 0xa8, 0x74, 0x2a,
        (byte) 0xc8, (byte) 0x96, 0x15, 0x4b, (byte) 0xa9, (byte) 0xf7,
        (byte) 0xb6, (byte) 0xe8, 0x0a, 0x54, (byte) 0xd7, (byte) 0x89, 0x6b,
        0x35};

    private CRC8() {

    }

    /**
     * Validates the CRC8 of a given 1-Wire adress.
     *
     * @param address The address to validate.
     * @return true if address is valid otherwise false.
     */
    public static boolean isAddressValid(long address) {
        byte crc8 = 0;
        for (int i = 0; i < 8; i++) {
            crc8 = CRC8_LOOKUP[(crc8 ^ ((int) (address >> (i * 8)))) & 0xFF];
        }
        return crc8 == 0;
    }

    /**
     * Calculates the CRC8 of the array, seed is 0.
     *
     * @param data the bytearray to compute the CRC8 from.
     * @return the CRC8 value.
     */
    public static byte crc8(byte[] data) {
        byte crc8 = 0;
        for (byte b : data) {
            crc8 = CRC8_LOOKUP[(crc8 ^ b) & 0x0FF];
        }
        return crc8;
    }

    /**
     * Calculates the CRC8 of the byte with given seed.
     *
     * @param data the bytearray to compute the CRC8 from.
     * @param seed the seed to use.
     * @return the CRC8 value.
     */
    public static byte crc8(byte data, byte seed) {
        return CRC8_LOOKUP[(seed ^ data) & 0x0FF];
    }

    /**
     * Calculates the CRC8 of the subarray with given seed.
     *
     * @param data the bytearray to compute the CRC8 from.
     * @param off the offset to start from.
     * @param len the length.
     * @param seed the seed to use.
     * @return the CRC8 value.
     */
    public static byte crc8(byte[] data, int off, int len, byte seed) {
        byte crc8 = seed;
        for (int i = 0; i < len; i++) {
            crc8 = CRC8_LOOKUP[(crc8 ^ data[i + off]) & 0x0FF];
        }
        return crc8;
    }

}
