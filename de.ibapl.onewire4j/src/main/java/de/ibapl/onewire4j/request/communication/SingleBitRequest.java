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
package de.ibapl.onewire4j.request.communication;

import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;

/**
 *
 * @author Arne Plöse
 */
public class SingleBitRequest extends CommunicationRequest<SingleBitResponse> {

    public DataToSend dataToSend;
    public OneWireSpeed speed;
    public boolean armPowerDelivery;

    public SingleBitRequest(OneWireSpeed speed, DataToSend dataToSend, boolean armPowerDelivery) {
        this.speed = speed;
        this.dataToSend = dataToSend;
        this.armPowerDelivery = armPowerDelivery;

    }

    public SingleBitRequest() {
    }

    @Override
    public int responseSize(StrongPullupDuration spd) {
        return 1;
    }

}
