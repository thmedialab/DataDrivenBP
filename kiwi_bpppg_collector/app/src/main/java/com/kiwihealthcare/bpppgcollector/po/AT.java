package com.kiwihealthcare.bpppgcollector.po;

public class AT {
	float amp;
	long abTime;

	public AT(float amp, long abTime) {
		this.amp = amp;
		this.abTime = abTime;
	}

	public long getTime() {
		return abTime;
	}

	public float getAmp() {
		return amp;
	}
}
