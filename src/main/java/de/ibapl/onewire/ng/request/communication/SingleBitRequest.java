package de.ibapl.onewire.ng.request.communication;

import de.ibapl.onewire.ng.request.OneWireRequest;
import de.ibapl.onewire.ng.request.Speed;

public class SingleBitRequest extends CommunicationRequest<SingleBitResponse> {
	public DataToSend dataToSend;
	public Speed speed;
	public boolean armPowerDelivery;

	public SingleBitRequest(Speed speed, DataToSend dataToSend, boolean armPowerDelivery) {
		this.speed = speed;
		this.dataToSend = dataToSend;
		this.armPowerDelivery = armPowerDelivery;
		
	}

	public SingleBitRequest() {
	}
}
