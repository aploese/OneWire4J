/*
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2018-2024, Arne Plöse and individual contributors as indicated
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
public enum SerialPortSpeed {
    SPS_9_6,
    SPS_19_2,
    SPS_57_6,
    SPS_115_2,
    SPS_9_6_I,
    SPS_19_2_I,
    SPS_57_6_I,
    SPS_115_2_I;

    @Override
    public String toString() {
        return switch (this) {
            case SPS_9_6 ->
                "9600 bps";
            case SPS_19_2 ->
                "19200 bps";
            case SPS_57_6 ->
                "57600 bps";
            case SPS_115_2 ->
                "115200 bps";
            case SPS_9_6_I ->
                "9600 bps (inverted)";
            case SPS_19_2_I ->
                "19200 bps (inverted)";
            case SPS_57_6_I ->
                "57600 bps (inverted)";
            case SPS_115_2_I ->
                "115200 bps (inverted)";
            default ->
                throw new IllegalArgumentException("Can't handle value: " + name());
        };
    }
}
