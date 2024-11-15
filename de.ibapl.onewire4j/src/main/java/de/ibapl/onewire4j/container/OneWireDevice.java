/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2017-2024, Arne Plöse and individual contributors as indicated
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

import de.ibapl.onewire4j.OneWireAdapter;
import java.io.IOException;

/**
 *
 * @author Arne Plöse
 */
public abstract class OneWireDevice implements OneWireContainer {

    private final long address;

    protected OneWireDevice(long address) {
        this.address = address;
    }

    /**
     * Factory method to create OneWire device from their address.
     *
     * @param address the address used to create the device.
     * @return
     */
    public static OneWireDevice fromAdress(long address) {
        return switch ((int) address & 0xff) {
            case 0x01 ->
                new OneWireDevice01(address);
            case 0x10 ->
                new OneWireDevice10(address);
            case 0x26 ->
                new OneWireDevice26(address);
            case 0x28 ->
                new OneWireDevice28(address);
            case 0x2d ->
                new OneWireDevice2d(address);
            default ->
                throw new RuntimeException("Can't handle One wire family: " + Integer.toHexString((int) address & 0xff));
        };
    }

    //TODO get a readTimeslot???? parasite power
    public void init(OneWireAdapter adapter) throws IOException {
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public String getAddressAsString() {
        return address2String(address);
    }

    public final static String address2String(long address) {
        return String.format("%08x", address);
    }

}
