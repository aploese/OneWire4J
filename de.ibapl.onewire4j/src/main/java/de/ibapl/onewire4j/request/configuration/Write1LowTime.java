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
package de.ibapl.onewire4j.request.configuration;

/**
 *
 * @author Arne Plöse
 */
public enum Write1LowTime {
    W1LT_8(8),
    W1LT_9(9),
    W1LT_10(10),
    W1LT_11(11),
    W1LT_12(12),
    W1LT_13(13),
    W1LT_14(14),
    W1LT_15(15);
    public final byte value;

    private Write1LowTime(final int value) {
        this.value = (byte) value;
    }

    @Override
    public String toString() {
        return value + " µs";
    }

}
