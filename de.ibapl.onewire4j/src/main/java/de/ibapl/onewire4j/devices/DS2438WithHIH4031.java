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
package de.ibapl.onewire4j.devices;

import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.OneWireDevice26;
import java.io.IOException;

/**
 *
 * @author aploese
 */
public class DS2438WithHIH4031 {

    final OneWireDevice26 ds2438;

    public DS2438WithHIH4031(OneWireDevice26 ds2438) {
        this.ds2438 = ds2438;
    }

    public double getHIH4031Humidity(OneWireAdapter adapter) throws IOException {
        final double VDD;
        final double VAD;
        final double T;

        OneWireDevice26.ScratchpadPage0Data data = ds2438.getScratchpadPage0(adapter, true, true);
        T = data.getTemperature();
        if (data.isVDD()) {
            VDD = data.getVoltage();
            data.setVDD(false);
            ds2438.setScratchpadPage0(adapter, data);
            data = ds2438.getScratchpadPage0(adapter, false, true);
            VAD = data.getVoltage();
        } else {
            VAD = data.getVoltage();
            data.setVDD(true);
            ds2438.setScratchpadPage0(adapter, data);
            data = ds2438.getScratchpadPage0(adapter, false, true);
            VDD = data.getVoltage();

        }
        return (((VAD / VDD) - 0.16) / 0.0062) / (1.0546 - 0.00216 * T);
    }

    public double getTemperature(OneWireAdapter adapter) throws IOException {
        OneWireDevice26.ScratchpadPage0Data page0 = ds2438.getScratchpadPage0(adapter, true, true);
        return page0.getTemperature();
    }

}
