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
package de.ibapl.onewire4j.container;

import de.ibapl.onewire4j.utils.CRC8;

/**
 *
 * @author Arne Plöse
 */
public interface OneWireContainer {

    public final static int ADDRESS_SIZE = 8;

    public static boolean isAsddressValid(long address) {
        return CRC8.isAddressValid(address);
        // TODO The DS28E04 has a pin selectable ROM ID input. However,
        // the CRC8 for the ROM ID assumes that the selecatable bits
        // are always 1.
    }

    public static String addressToString(long address) {
        return String.format("%08x", address);
    }

    public static byte[] arrayOfAddress(final long address) {
        final byte[] result = new byte[ADDRESS_SIZE];
        result[0] = (byte) (address & 0xFF);
        result[1] = (byte) ((address >> 8) & 0xFF);
        result[2] = (byte) ((address >> 16) & 0xFF);
        result[3] = (byte) ((address >> 24) & 0xFF);
        result[4] = (byte) ((address >> 32) & 0xFF);
        result[5] = (byte) ((address >> 40) & 0xFF);
        result[6] = (byte) ((address >> 48) & 0xFF);
        result[7] = (byte) ((address >> 56) & 0xFF);
        return result;
    }

    public static long addressOf(final byte[] address) {
        if (address.length != ADDRESS_SIZE) {
            throw new IllegalArgumentException("Address size mismatch");
        }
        long result = (long) (address[7] & 0xFF) << 56;
        result |= (long) (address[6] & 0xFF) << 48;
        result |= (long) (address[5] & 0xFF) << 40;
        result |= (long) (address[4] & 0xFF) << 32;
        result |= (long) (address[3] & 0xFF) << 24;
        result |= (long) (address[2] & 0xFF) << 16;
        result |= (long) (address[1] & 0xFF) << 8;
        result |= address[0] & 0xFF;
        return result;
    }

    long getAddress();

    String getAddressAsString();
}
