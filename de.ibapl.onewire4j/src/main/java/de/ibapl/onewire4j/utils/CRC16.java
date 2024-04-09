/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2019-2024, Arne Plöse and individual contributors as indicated
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
 *
 * @author aploese
 */
public class CRC16 {

    public final static short CRC16_OF_SEEDS_ONE_COMPLEMENT = (short) 0xb001;
    public final static short DEFAULT_SEED = 0x0000;

    private static final boolean[] ODD_PARITY
            = {false, true, true, false, true, false, false, true,
                true, false, false, true, false, true, true, false};

    private short currentCrc16;

    public CRC16() {
        currentCrc16 = DEFAULT_SEED;
    }

    public short crc16(byte dataToCrc) {
        short dat = (short) ((dataToCrc ^ currentCrc16) & 0x00FF);

        currentCrc16 >>>= 8; //seed is promoted to int, and therefor this behaves like the >> operatur
        currentCrc16 &= 0xff; //so we must clean the upper bits after the shift... (see jls-5.6.1-100-C)

        if (ODD_PARITY[(dat & 0x0F)] ^ ODD_PARITY[(dat >>> 4)]) {
            currentCrc16 ^= 0xC001;
        }

        dat <<= 6;
        currentCrc16 ^= dat;
        dat <<= 1;
        currentCrc16 ^= dat;

        return currentCrc16;
    }

    public short crc16(byte dataToCrc[]) {
        // loop to do the crc on each data element
        for (byte i : dataToCrc) {
            crc16(i);
        }
        return currentCrc16;
    }

    public short crc16(byte[] data, int from, int len) {
        final int to = from + len;
        for (int i = from; i < to; i++) {
            crc16(data[i]);
        }
        return currentCrc16;
    }

    public short crc16(byte[] data, int from) {
        for (int i = from; i < data.length; i++) {
            crc16(data[i]);
        }
        return currentCrc16;
    }

    /**
     * @return the currentCrc16
     */
    public short getCurrentCrc16() {
        return currentCrc16;
    }

    /**
     * @param seed the currentCrc16 to set
     */
    public void setCurrentCrc16(short seed) {
        this.currentCrc16 = seed;
    }

    public void resetCurrentCrc16() {
        currentCrc16 = DEFAULT_SEED;
    }

    public boolean isOneComplement() {
        return currentCrc16 == CRC16_OF_SEEDS_ONE_COMPLEMENT;
    }
}
