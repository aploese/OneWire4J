package de.ibapl.onewire.ng.request;

/**
 * A 1-wire command request or response
 * 
 * @author aploese
 */
public class OneWireRequest<R> {
	public enum RequestState {
		READY_TO_SEND,
		WAIT_FOR_RESPONSE,
		SUCCESS;
	}
	
	public RequestState requestState = RequestState.READY_TO_SEND;

    public R response;

    public OneWireRequest<R> resetState() {
		this.requestState = RequestState.READY_TO_SEND;
    	return this;
	}

}
