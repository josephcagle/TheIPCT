package com.josephcagle.ipct.client.gui;

public class NameAndIPGetter {

	/*
	 * Asks the user for username and server IP address using
	 * a custom (IPCT-specific) dialog.
	 */
	public String[] get() {
		return new GetInfoFrame().getNameAndIP();
	}

}

