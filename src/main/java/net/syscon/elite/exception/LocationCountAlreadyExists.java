package net.syscon.elite.exception;


@SuppressWarnings("serial")
public class LocationCountAlreadyExists extends Exception {

	public LocationCountAlreadyExists() {
	}

	public LocationCountAlreadyExists(final String s) {
		super(s);
	}

	public LocationCountAlreadyExists(final String s, final Throwable throwable) {
		super(s, throwable);
	}

	public LocationCountAlreadyExists(final Throwable throwable) {
		super(throwable);
	}

	public LocationCountAlreadyExists(final String s, final Throwable throwable, final boolean b, final boolean b1) {
		super(s, throwable, b, b1);
	}
}
