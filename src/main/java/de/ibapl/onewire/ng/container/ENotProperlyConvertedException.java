package de.ibapl.onewire.ng.container;

public class ENotProperlyConvertedException extends Exception {
	private final double value;
	
	public double getValue() {
		return value;
	}

	public ENotProperlyConvertedException(double value) {
		this.value = value;
	}
}
