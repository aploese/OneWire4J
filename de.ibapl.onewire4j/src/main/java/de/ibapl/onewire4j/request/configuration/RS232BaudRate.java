package de.ibapl.onewire4j.request.configuration;

/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 Arne Plöse
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

/**
 *
 * @author aploese
 */
public enum RS232BaudRate {
    RBR_9_6,
    RBR_19_2,
    RBR_57_6,
    RBR_115_2,
    RBR_9_6_I,
    RBR_19_2_I,
    RBR_57_6_I,
    RBR_115_2_I;
    
    public String toString() {
        switch (this) {
            case RBR_9_6: return "9600 bps";
            case RBR_19_2: return "19200 bps";
            case RBR_57_6: return "57600 bps";
            case RBR_115_2: return "115200 bps";
            case RBR_9_6_I: return "9600 bps (inverted)";
            case RBR_19_2_I: return "19200 bps (inverted)";
            case RBR_57_6_I: return "57600 bps (inverted)";
            case RBR_115_2_I: return "115200 bps (inverted)";
            default: throw new RuntimeException("cant ahndle value: " + name());
        }
    }
}
