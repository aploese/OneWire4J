package de.ibapl.onewire.ng.container;

import java.io.IOException;
import java.time.Instant;

import de.ibapl.onewire.ng.Encoder;
import de.ibapl.onewire.ng.cli.OneWireAdapter;
import de.ibapl.onewire.ng.request.OneWireRequest;
import de.ibapl.onewire.ng.request.communication.BitResult;
import de.ibapl.onewire.ng.request.communication.DataToSend;
import de.ibapl.onewire.ng.request.communication.SingleBitRequest;
import de.ibapl.onewire.ng.request.configuration.StrongPullupDuration;
import de.ibapl.onewire.ng.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire.ng.request.data.RawDataRequest;
import de.ibapl.onewire.ng.request.data.ReadBytesRequest;
import de.ibapl.onewire.utils.CRC8;

public interface TemperatureContainer extends OneWireContainer {

	@OneWireDataCommand
	public final byte READ_POWER_SUPPLY_CMD = (byte) 0xb4;

	@OneWireDataCommand
	public final byte CONVERT_TEMPERATURE_CMD = (byte) 0x44;

	/**
	 * Sends convert to all devices....
	 */
	public static Instant sendDoConvertRequestToAll(OneWireAdapter adapter, boolean parasitePowerNeeded)
			throws IOException {
		adapter.sendSkipRomRequest();
		final Instant ts = Instant.now();

		if (parasitePowerNeeded) {
			adapter.sendByteWithPower(CONVERT_TEMPERATURE_CMD, StrongPullupDuration.SPUD_POSITIVE_INFINITY,
					adapter.getSpeedFromBaudrate());
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			adapter.sendTerminatePulse();
		} else {
			adapter.sendCommand(new DataRequestWithDeviceCommand(CONVERT_TEMPERATURE_CMD, new byte[0]));
			final ReadBytesRequest r = new ReadBytesRequest(1);
			while (adapter.sendCommand(r)[0] != (byte) 0xff) {
				r.resetState();
				try {
					Thread.sleep(100);
				} catch (InterruptedException  e) {
					//no-op
				}
			}
		}
		return ts;
	}

	public static boolean isParasitePower(OneWireAdapter adapter) throws IOException {
		adapter.sendSkipRomRequest();
		
		DataRequestWithDeviceCommand readPowerSupplyRequest = new DataRequestWithDeviceCommand(READ_POWER_SUPPLY_CMD,
				new byte[] { (byte) 0xff });
		adapter.sendCommand(readPowerSupplyRequest);
		return readPowerSupplyRequest.response[0] != (byte) 0xff;
	}

	default void sendDoConvertRequest(OneWireAdapter adapter) throws IOException {
		adapter.sendMatchRomRequest(getAddress());

		adapter.sendByteWithPower(CONVERT_TEMPERATURE_CMD, StrongPullupDuration.SPUD_POSITIVE_INFINITY,
				adapter.getSpeedFromBaudrate());

		try {
			Thread.sleep(750);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		adapter.sendTerminatePulse();
		// TODO READ Bit???
		byte b = adapter.sendReadByteRequest();
		if ((b & 0xff) != 0xFF) {
			throw new RuntimeException();
		}
	}

	default void readScratchpad(OneWireAdapter adapter, ReadScratchpadRequest request) throws IOException {
		adapter.sendMatchRomRequest(getAddress());
		adapter.sendCommand(request.resetState());
		if (CRC8.compute(request.response) != 0) {
			throw new IOException("CRC mismatch");
		}
	}

	default double convertAndReadTemperature(OneWireAdapter adapter)
			throws IOException, ENotProperlyConvertedException {
		final ReadScratchpadRequest request = new ReadScratchpadRequest();
		sendDoConvertRequest(adapter);
		readScratchpad(adapter, request);
		return getTemperature(request);
	}

	default double getTemperature(ReadScratchpadRequest request) throws ENotProperlyConvertedException {
		return request.response[2];
	}

	default double getAlarmTempLowerLimit(ReadScratchpadRequest request) throws ENotProperlyConvertedException {
		return request.response[3];
	}

}
