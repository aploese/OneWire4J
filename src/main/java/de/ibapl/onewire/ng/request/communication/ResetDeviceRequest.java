package de.ibapl.onewire.ng.request.communication;

import de.ibapl.onewire.ng.request.Speed;

public class ResetDeviceRequest extends CommunicationRequest<ResetDeviceResponse> {
	public Speed speed;

	public static ResetDeviceRequest of(Speed speed) {
		final ResetDeviceRequest result = new ResetDeviceRequest();
		result.speed = speed;
		return result;
	} 
}
