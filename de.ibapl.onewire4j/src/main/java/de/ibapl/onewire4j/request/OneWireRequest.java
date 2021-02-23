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
package de.ibapl.onewire4j.request;

import de.ibapl.onewire4j.Decoder;
import de.ibapl.onewire4j.Encoder;
import de.ibapl.onewire4j.request.configuration.StrongPullupDuration;

/**
 * A 1-wire command request with response.
 *
 * @author Arne Plöse
 *
 * @param <R> the type of the response.
 */
public abstract class OneWireRequest<R> {

    /**
     * The internal state of this request.
     *
     * @author aploese
     *
     */
    public enum RequestState {
        READY_TO_SEND,
        WAIT_FOR_RESPONSE,
        SUCCESS;
    }

    /**
     * This will be set by the {@linkplain Encoder} or {@linkplain Decoder} to
     * mark the current state of that request.
     *
     */
    protected RequestState requestState = RequestState.READY_TO_SEND;

    public R response;

    public final int readTimeSlots;

    public OneWireRequest(int readTimeSlots) {
        if (readTimeSlots < 0) {
            throw new IllegalArgumentException("read time slots must not be nagative!");
        }
        this.readTimeSlots = readTimeSlots;
    }

    /**
     * Sets the internal state to {@linkplain RequestState#READY_TO_SEND}.
     *
     * @return {@code this} for method chaining.
     */
    public OneWireRequest<R> resetState() {
        this.requestState = RequestState.READY_TO_SEND;
        return this;
    }

    /**
     * Sets the internal state to {@linkplain RequestState#SUCCESS}.
     */
    public void success() {
        requestState = RequestState.SUCCESS;
    }

    /**
     * Checks state and throws IllegalArgumentException if not in
     * expectedRequestState.
     *
     * @param expectedRequestState the expected {@linkplain RequestState}
     *
     * @throws IllegalArgumentException if not in requestState.
     */
    public void throwIfNot(RequestState expectedRequestState) {
        if (this.requestState != expectedRequestState) {
            throw new IllegalStateException("Request state not: " + expectedRequestState + " but was: " + this.requestState);
        }
    }

    /**
     * Sets the internal state to {@linkplain RequestState#WAIT_FOR_RESPONSE}.
     */
    public void waitForResponse() {
        requestState = RequestState.WAIT_FOR_RESPONSE;
    }

    public abstract int responseSize(StrongPullupDuration spd);
}
