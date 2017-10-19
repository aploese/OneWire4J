package de.ibapl.onewire.ng;

import java.util.Arrays;

import de.ibapl.onewire.ng.request.data.RawDataRequest;

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
				throw new RuntimeException("Error Nothnig found");
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
