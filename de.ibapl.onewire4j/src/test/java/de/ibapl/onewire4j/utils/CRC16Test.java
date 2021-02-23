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
package de.ibapl.onewire4j.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author aploese
 */
public class CRC16Test {

    private CRC16 crc16 = new CRC16();

    @BeforeEach
    public void beforeAll() {
        crc16.resetCurrentCrc16();
    }

    public CRC16Test() {
    }

    @Test
    public void testOneComplement() throws Exception {
        byte[] data = new byte[2];
        for (int value = 0; value <= 0xffff; value++) {
            final int valueInverted = ~value;
            data[1] = (byte) ((valueInverted >>> 8) & 0xff);
            data[0] = (byte) (valueInverted & 0xff);
            crc16.setCurrentCrc16((short) value);
            crc16.crc16(data);
            assertTrue(crc16.isOneComplement(), String.format("@ 0x%04x", value));
        }
    }

    @Test
    public void testSameInputAsSeed() throws Exception {
        byte[] data = new byte[2];
        for (int value = 0; value <= 0xffff; value++) {
            data[1] = (byte) ((value >>> 8) & 0xff);
            data[0] = (byte) (value & 0xff);
            crc16.setCurrentCrc16((short) value);
            assertEquals((short) 0x0000, crc16.crc16(data), String.format("@ 0x%04x", value));
        }
    }

    @Test
    public void testCrc16_1() {
        crc16.crc16((byte) 0x0f); //Command
        crc16.crc16(new byte[]{0x08, 0x00, 0x11, 0x48, 0x02, 0x03, 0x04, 0x05, 0x60, (byte) 0xf0}); // write scratchpad
        crc16.crc16(new byte[]{0x3a, 0x52}); // crc16 from 2431
        assertTrue(crc16.isOneComplement());
    }

}
