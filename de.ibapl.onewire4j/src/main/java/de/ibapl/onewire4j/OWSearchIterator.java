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
package de.ibapl.onewire4j;

import de.ibapl.onewire4j.request.data.RawDataRequest;
import java.util.Arrays;

/**
 *
 * @author Arne Plöse
 */
public class OWSearchIterator {

    private int searchLastDiscrepancy = 0xFF;

    private boolean searchFinished;

    private long addr;

    public void interpretSearch(final RawDataRequest searchCommandData) {

        addr = 0;
        // set the temp Last Descrep to none
        int temp_last_descrepancy = 0xFF;

        // interpret the search response sequence
        for (int i = 0; i < 64; i++) {
            final boolean discrepancyFlag = bitRead(searchCommandData.response, i * 2);
            final boolean choosenPathFlag = bitRead(searchCommandData.response, i * 2 + 1);

            // get the SerialNum bit
            if (choosenPathFlag) {
                addr |= 1L << i;
            }

            // check LastDiscrepancy
            if (discrepancyFlag && !choosenPathFlag) {
                temp_last_descrepancy = i + 1;
            }
        }

        if (temp_last_descrepancy == 63) {
            throw new RuntimeException("Error Nothing found request: " + searchCommandData);
        }
        // check for the last one
        if ((temp_last_descrepancy == searchLastDiscrepancy) || (temp_last_descrepancy == 0xFF)) {
            searchFinished = true;
        }

        // set the count
        searchLastDiscrepancy = temp_last_descrepancy;

        // only modify bits if not the first search
        if (searchLastDiscrepancy != 0xFF) {
            Arrays.fill(searchCommandData.requestData, (byte) 0);

            // set the bits in the added buffer
            for (int i = 0; i < searchLastDiscrepancy - 1; i++) {
                // before last discrepancy: go direction based on ID choosenPathFlag in response
                if (bitRead(searchCommandData.response, i * 2 + 1)) {
                    setBit(searchCommandData.requestData, (i * 2 + 1));
                }
            }
            // at last discrepancy (go 1's direction) the rest are zeros
            setBit(searchCommandData.requestData, ((searchLastDiscrepancy - 1) * 2 + 1));
        }
    }

    private boolean bitRead(final byte[] bitBuffer, final int address) {
        return ((bitBuffer[address / 8] >> address % 8) & 0x01) == 0x01;
    }

    private void setBit(final byte[] bitBuffer, final int address) {
        bitBuffer[address / 8] |= 0x01 << address % 8;
    }

    public boolean isSearchFinished() {
        return searchFinished;
    }

    public long getAddress() {
        return addr;
    }

}
