package de.ibapl.onewire.ng.request.communication;

import de.ibapl.onewire.ng.request.Speed;
import de.ibapl.onewire.ng.request.VoidResponse;

public class SearchAcceleratorCommand extends CommunicationRequest<VoidResponse> {

	public static SearchAcceleratorCommand of(SearchAccelerator accelerator, Speed speed) {
		final SearchAcceleratorCommand result = new SearchAcceleratorCommand();
		result.searchAccelerator = accelerator;
		result.speed = speed;
		return result;
	}

	public SearchAccelerator searchAccelerator;
	public Speed speed;
	
}
