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
package de.ibapl.onewire4j.request.configuration;

/**
 *
 * @author Arne Plöse
 */
public enum StrongPullupDuration {
    SPUD_16_4,
    SPUD_65_5,
    SPUD_131,
    SPUD_262,
    SPUD_524,
    SPUD_1048,
    SPUD_DYN,
    SPUD_POSITIVE_INFINITY;


    @Override
	public String toString() {
        switch (this) {
            case SPUD_16_4: return 16.4 + " µs";
            case SPUD_65_5: return 65.5 + " µs";
            case SPUD_131: return "131 µs";
            case SPUD_262: return "262 µs";
            case SPUD_1048: return "1048 µs";
            case SPUD_DYN: return "dyn.";
            case SPUD_POSITIVE_INFINITY: return "∞";
            default: throw new RuntimeException("can't handle: " + name());
        }
    }
}
