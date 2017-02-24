package net.syscon.elite.service.exception;


public class LocationCountAlreadyExists extends Exception {

	public LocationCountAlreadyExists() {
	}

	public LocationCountAlreadyExists(String s) {
		super(s);
	}

	public LocationCountAlreadyExists(String s, Throwable throwable) {
		super(s, throwable);
	}

	public LocationCountAlreadyExists(Throwable throwable) {
		super(throwable);
	}

	public LocationCountAlreadyExists(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
