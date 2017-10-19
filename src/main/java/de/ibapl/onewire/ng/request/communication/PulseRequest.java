package de.ibapl.onewire.ng.request.communication;

public class PulseRequest extends CommunicationRequest<PulseResponse> {
	
	public static PulseRequest of(PulsePower pulsePower, PulseType pulseType) {
		PulseRequest result = new PulseRequest();
		result.pulsePower = pulsePower;
		result.pulseType =pulseType;
		return result;
	}
	
	public PulsePower pulsePower;
	public PulseType pulseType;
	
}
