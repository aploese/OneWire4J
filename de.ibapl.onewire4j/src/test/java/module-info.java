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
module de.ibapl.onewire4j.test {

    requires de.ibapl.onewire4j;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    uses de.ibapl.spsw.api.SerialPortSocketFactory;

    exports de.ibapl.onewire4j.test;
    exports de.ibapl.onewire4j.test.container;
    exports de.ibapl.onewire4j.test.network;
    exports de.ibapl.onewire4j.test.utils;

}
