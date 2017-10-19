/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibapl.onewire.ng;

import de.ibapl.onewire.ng.container.OneWireDataCommand;
import de.ibapl.onewire.ng.request.CommandRequest;
import de.ibapl.onewire.ng.request.OneWireRequest;
import de.ibapl.onewire.ng.request.PulseTerminationRequest;
import de.ibapl.onewire.ng.request.Speed;
import de.ibapl.onewire.ng.request.OneWireRequest.RequestState;
import de.ibapl.onewire.ng.request.communication.CommunicationRequest;
import de.ibapl.onewire.ng.request.communication.PulseRequest;
import de.ibapl.onewire.ng.request.communication.ResetDeviceRequest;
import de.ibapl.onewire.ng.request.communication.SearchAcceleratorCommand;
import de.ibapl.onewire.ng.request.communication.SingleBitRequest;
import de.ibapl.onewire.ng.request.configuration.ConfigurationReadRequest;
import de.ibapl.onewire.ng.request.configuration.ConfigurationRequest;
import de.ibapl.onewire.ng.request.configuration.ConfigurationWriteRequest;
import de.ibapl.onewire.ng.request.configuration.DataSampleOffsetAndWrite0RecoveryTime;
import de.ibapl.onewire.ng.request.configuration.LoadSensorThreshold;
import de.ibapl.onewire.ng.request.configuration.ProgrammingPulseDuration;
import de.ibapl.onewire.ng.request.configuration.PullDownSlewRateParam;
import de.ibapl.onewire.ng.request.configuration.RS232BaudRate;
import de.ibapl.onewire.ng.request.configuration.StrongPullupDuration;
import de.ibapl.onewire.ng.request.configuration.Write1LowTime;
import de.ibapl.onewire.ng.request.data.DataRequest;
import de.ibapl.onewire.ng.request.data.DataRequestWithDeviceCommand;
import de.ibapl.onewire.ng.request.data.RawDataRequest;
import de.ibapl.onewire.ng.request.data.SearchCommand;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author aploese
 */
public class Encoder {

	public static final byte SWITCH_TO_DATA_MODE_BYTE = (byte) 0xe1;
	public static final byte SWITCH_TO_COMMAND_MODE_BYTE = (byte) 0xe3;
	@OneWireDataCommand
	public static final byte MATCH_ROM_CMD = 0x55;
	@OneWireDataCommand
	public static final byte SKIP_ROM_CMD = (byte)0xcc;
	public static final byte RESET_CMD = (byte)0xC1;

	private final OutputStream os;

	public Encoder(OutputStream os) {
		this.os = os;
	}

	public <R> void encode(OneWireRequest<R> request) throws IOException {
		if (request.requestState != RequestState.READY_TO_SEND) {
			throw new RuntimeException("Wrong state");
		}
		if (request instanceof CommandRequest) {
			if (request instanceof ConfigurationRequest) {
				if (request instanceof ConfigurationReadRequest) {
					os.write(encodeConfigurationReadRequest((ConfigurationReadRequest<R>) request));
				} else if (request instanceof ConfigurationWriteRequest) {
					os.write(encodeConfigurationWriteRequest((ConfigurationWriteRequest<R>) request));
				} else {
					throw new RuntimeException("Unknown subtype of ConfigurationRequest: " + request.getClass());
				}
			} else if (request instanceof CommunicationRequest) {
				if (request instanceof SingleBitRequest) {
					os.write(encodeSingleBitSendCommand((SingleBitRequest) request));
				} else if (request instanceof SearchAcceleratorCommand) {
					os.write(encodeSearchAcceleratorCommand((SearchAcceleratorCommand) request));
				} else if (request instanceof ResetDeviceRequest) {
					os.write(encodeResetDevice((ResetDeviceRequest) request));
				} else if (request instanceof PulseRequest) {
					os.write(encodePulseRequest((PulseRequest)request));
				} else if (request instanceof PulseTerminationRequest) {
					os.write(0xF1);
				} else {
					throw new RuntimeException("Unknown subtype of CommunicationRequest: " + request.getClass());
				}
			} else {
				throw new RuntimeException("Unknown subtype of CommandRequest: " + request.getClass());
			}
		} else if (request instanceof DataRequest) {
			if (request instanceof SearchCommand) {
				os.write(0xf0);
			} else if (request instanceof RawDataRequest) {
				writeDataBytes(((RawDataRequest)request).requestData);
			} else if (request instanceof DataRequestWithDeviceCommand) {
				final DataRequestWithDeviceCommand r = (DataRequestWithDeviceCommand)request;
				os.write(r.command);
				writeDataBytes(r.requestData);
			} else {
				throw new RuntimeException("NOT IMPLEMENTED: " + request.getClass());
			}
		} else {
			throw new RuntimeException("Unknown subtype of CommandRequest: " + request.getClass());
		}
		request.requestState = RequestState.WAIT_FOR_RESPONSE;
	}

	private void writeDataBytes(final byte[] requestData) throws IOException {
		int lastWriteMark = 0;
		for (int i = 0; i < requestData.length; i++ ) {
			if (requestData[i] == SWITCH_TO_COMMAND_MODE_BYTE) {
				os.write(requestData, lastWriteMark, i - lastWriteMark);
				os.write(requestData[i]);
				lastWriteMark = i;
			}
		}
		os.write(requestData, lastWriteMark, requestData.length - lastWriteMark);
	}

	private int encodePulseRequest(PulseRequest request) {
		int data = 0b111_0_11_0_1;
		switch (request.pulsePower) {
		case PROGRAMMING_PULSE:
			data |= 0b000_1_00_0_0;
			break;
		case STRONG_PULLUP:
			break;
		default:
			throw new RuntimeException();
		}	
		switch (request.pulseType) {
		case ARM_AFTER_EVERY_BYTE:
			data |= 0b000_0_00_1_0;
			break;
		case DISARM:
			break;
		default:
			throw new RuntimeException();
		}	
		return data;
	}

	private int encodeResetDevice(ResetDeviceRequest request) {
		return encodeSpeed(request.speed) | 0b1100_00_01; 
	}

	private int encodeSpeed(Speed speed) {
		switch (speed) {
		case STANDARD:
			return 0b0000_00_00;
		case FLEX:
			return 0b0000_01_00;
		case OVERDRIVE:
			return 0b0000_10_00;
		case STANDARD_11:
			return 0b0000_11_00;
		default:
			throw new RuntimeException("Unknown speed: " + speed);
		}
	}

	private int encodeSearchAcceleratorCommand(SearchAcceleratorCommand request) {
		switch (request.searchAccelerator) {
		case ON:
			return encodeSpeed(request.speed) | 0b1011_00_01;
		case OFF:
			return encodeSpeed(request.speed) | 0b1010_00_01;
		default:
			throw new RuntimeException("Unknown search accelerator: " + request.searchAccelerator);
		} 	
	}

	int encodeSingleBitSendCommand(SingleBitRequest request) throws IOException {
		switch (request.dataToSend) {
		case WRITE_0_BIT:
			return encodeSpeed(request.speed) | (request.armPowerDelivery ? 0b100_0_00_11 : 0b100_0_00_01);
		case WRITE_1_OR_READ_BIT:
			return encodeSpeed(request.speed) | (request.armPowerDelivery ?  0b100_1_00_11 : 0b100_1_00_01);
		default:
			throw new RuntimeException("Unknown dataToSend: " + request.dataToSend);
		}
	}

	int encodeConfigurationReadRequest(ConfigurationReadRequest<?> configurationCommand) throws IOException {
		switch (configurationCommand.commandType) {
		case PDSRC:
			return 0b0_000_001_1;
		case PPD:
			return 0b0_000_010_1;
		case SPUD:
			return 0b0_000_011_1;
		case W1LT:
			return 0b0_000_100_1;
		case DSO_AND_W0RT:
			return 0b0_000_101_1;
		case LST:
			return 0b0_000_110_1;
		case RBR:
			return 0b0_000_111_1;
		default:
			throw new RuntimeException("Unknown Configuration command: " + configurationCommand.commandType);
		}
	}

	@SuppressWarnings("unchecked")
	int encodeConfigurationWriteRequest(ConfigurationWriteRequest<?> configurationCommand) throws IOException {
		switch (configurationCommand.commandType) {
		case PDSRC:
			switch (((ConfigurationWriteRequest<PullDownSlewRateParam>) configurationCommand).propertyValue) {
			case PDSRC_15:
				return 0b0_001_000_1;
			case PDSRC_2_2:
				return 0b0_001_001_1;
			case PDSRC_1_65:
				return 0b0_001_010_1;
			case PDSRC_1_37:
				return 0b0_001_011_1;
			case PDSRC_1_1:
				return 0b0_001_100_1;
			case PDSRC_0_83:
				return 0b0_001_101_1;
			case PDSRC_0_7:
				return 0b0_001_110_1;
			case PDSRC_0_55:
				return 0b0_001_111_1;
			default:
				throw new RuntimeException("Cant't handle PDSRC: " + configurationCommand.propertyValue);
			}
		case PPD:
			switch (((ConfigurationWriteRequest<ProgrammingPulseDuration>) configurationCommand).propertyValue) {
			case PPD_32:
				return 0b0_010_000_1;
			case PPD_64:
				return 0b0_010_001_1;
			case PPD_128:
				return 0b0_010_010_1;
			case PPD_256:
				return 0b0_010_011_1;
			case PPD_512:
				return 0b0_010_100_1;
			case PPD_1024:
				return 0b0_010_101_1;
			case PPD_2048:
				return 0b0_010_110_1;
			case PPD_POSITIVE_INFINITY:
				return 0b0_010_111_1;
			default:
				throw new RuntimeException("Cant't handle PPD: " + configurationCommand.propertyValue);
			}
		case SPUD:
			switch (((ConfigurationWriteRequest<StrongPullupDuration>) configurationCommand).propertyValue) {
			case SPUD_16_4:
				return 0b0_011_000_1;
			case SPUD_65_5:
				return 0b0_011_001_1;
			case SPUD_131:
				return 0b0_011_010_1;
			case SPUD_262:
				return 0b0_011_011_1;
			case SPUD_524:
				return 0b0_011_100_1;
			case SPUD_1048:
				return 0b0_011_101_1;
			case SPUD_DYN:
				return 0b0_011_110_1;
			case SPUD_POSITIVE_INFINITY:
				return 0b0_011_111_1;
			default:
				throw new RuntimeException("Cant't handle SPUD: " + configurationCommand.propertyValue);
			}
		case W1LT:
			switch (((ConfigurationWriteRequest<Write1LowTime>) configurationCommand).propertyValue) {
			case W1LT_8:
				return 0b0_100_000_1;
			case W1LT_9:
				return 0b0_100_001_1;
			case W1LT_10:
				return 0b0_100_010_1;
			case W1LT_11:
				return 0b0_100_011_1;
			case W1LT_12:
				return 0b0_100_100_1;
			case W1LT_13:
				return 0b0_100_101_1;
			case W1LT_14:
				return 0b0_100_110_1;
			case W1LT_15:
				return 0b0_100_111_1;
			default:
				throw new RuntimeException("Cant't handle W1LT: " + configurationCommand.propertyValue);
			}
		case DSO_AND_W0RT:
			switch (((ConfigurationWriteRequest<DataSampleOffsetAndWrite0RecoveryTime>) configurationCommand).propertyValue) {
			case DSO_AND_W0RT_3:
				return 0b0_101_000_1;
			case DSO_AND_W0RT_4:
				return 0b0_101_001_1;
			case DSO_AND_W0RT_5:
				return 0b0_101_010_1;
			case DSO_AND_W0RT_6:
				return 0b0_101_011_1;
			case DSO_AND_W0RT_7:
				return 0b0_101_100_1;
			case DSO_AND_W0RT_8:
				return 0b0_101_101_1;
			case DSO_AND_W0RT_9:
				return 0b0_101_110_1;
			case DSO_AND_W0RT_10:
				return 0b0_101_111_1;
			default:
				throw new RuntimeException("Cant't handle DSO_AND_W0RT: " + configurationCommand.propertyValue);
			}
		case LST:
			switch (((ConfigurationWriteRequest<LoadSensorThreshold>) configurationCommand).propertyValue) {
			case LST_1_8:
				return 0b0_110_000_1;
			case LST_2_1:
				return 0b0_110_001_1;
			case LST_2_4:
				return 0b0_110_010_1;
			case LST_2_7:
				return 0b0_110_011_1;
			case LST_3_0:
				return 0b0_110_100_1;
			case LST_3_3:
				return 0b0_110_101_1;
			case LST_3_6:
				return 0b0_110_110_1;
			case LST_3_9:
				return 0b0_110_111_1;
			default:
				throw new RuntimeException("Cant't handle LST: " + configurationCommand.propertyValue);
			}
		case RBR:
			switch (((ConfigurationWriteRequest<RS232BaudRate>) configurationCommand).propertyValue) {
			case RBR_9_6:
			case RBR_19_2:
				return 0b0_111_001_1;
			case RBR_57_6:
				return 0b0_111_010_1;
			case RBR_115_2:
				return 0b0_111_011_1;
			case RBR_9_6_I:
				return 0b0_111_100_1;
			case RBR_19_2_I:
				return 0b0_111_101_1;
			case RBR_57_6_I:
				return 0b0_111_110_1;
			case RBR_115_2_I:
				return 0b0_111_111_1;
			default:
				throw new RuntimeException("Cant't handle RBR: " + configurationCommand.propertyValue);
			}
		default:
			throw new RuntimeException("Unknown Configuration command: " + configurationCommand.commandType);
		}
	}

}

//beide   f04002880202aaaa280200800000000aa2
//erster  f0000882a2080a8a88020000000000a2a2
//zweiter f00002880202aaaa280200800000000aa2
/*
0x44 mit pullup auf 5V bitweise...
"e38585958585859587"
"8484978484849784"
*/
