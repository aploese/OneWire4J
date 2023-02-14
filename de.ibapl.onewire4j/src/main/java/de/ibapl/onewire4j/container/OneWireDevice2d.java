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

/**
 *
 * @author Arne Plöse
 */
@DeviceInfo(oneWireName = "DS2431", iButtonName = "DS1972")
public class OneWireDevice2d extends OneWireDevice implements MemoryBankContainer {

    public final static int PAGES = 4;
    public final static int PAGE_SIZE_IN_BYTE = 32;

    public final static int REGISTER_START_ADDRESS = 0x80;

    public final static byte WRITE_PROTECT = 0x55;
    public final static byte EPROM_MODE = (byte) 0xaa;

    public OneWireDevice2d(long address) {
        super(address);
    }

}
