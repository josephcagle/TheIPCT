package com.josephcagle.ipct.client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import com.josephcagle.ipct.client.gui.ChatFrame;
import com.josephcagle.ipct.client.gui.NameAndIPGetter;

public class IPCTClient {

	private String username = "NONE";
	private String serverIPAddress = null;
	private ChatFrame chatFrame;

	private volatile boolean running = true;  // shut down gracefully later
	
	public static void main(String[] args) {
		new IPCTClient();
	}
	
	public IPCTClient() {
		
		String[] result = new NameAndIPGetter().get();
		
		if (result[0].equals("IPCT_CLIENT"))   // protect against wannabe hackers
			result[0] = "nobodyinparticular";  // ( the String[] is { name, serverIP } )
		
		this.username = result[0];
		this.serverIPAddress = result[1];

		chatFrame = new ChatFrame();
		chatFrame.setUsername(this.username);
		chatFrame.setVisible(true);

		try (Socket s = new Socket(this.serverIPAddress, 23435);
				PrintStream toServer = new PrintStream(s.getOutputStream());
				BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

//			if (!s.isConnected()) {
//				failGracefully();
//				return;
//			}

			MAIN_SERVER_IO: {
			
				String input = "NO_INPUT";

				toServer.println("IPCT_CLIENT:CONNECTION_REQUEST");
				toServer.println("IPCT_CLIENT:USERNAME/" + this.username);

				input = fromServer.readLine();
				if (!input.equals("IPCT_SERVER:CONNECTION_ACCEPTED") ) {
					toServer.println("IPCT_SERVER:ILLEGAL_INPUT");
					break MAIN_SERVER_IO;
				}

				input = fromServer.readLine();
				if (!input.equals("IPCT_SERVER:SENDING_CURRENT_CONVERSATION") ) {
					toServer.println("IPCT_SERVER:ILLEGAL_INPUT");
					break MAIN_SERVER_IO;
				}

				while ( ! (input = fromServer.readLine()).equals("IPCT_SERVER:END_OF_CONVERSATION")) {
					chatFrame.displayMessage(input);
				}

				while (running) {
					try {  // sleep 1/20th second
						Thread.sleep(80);  // don't waste processor time; people can't type as fast as a loop
					} catch (Exception e) { e.printStackTrace(); }

					if (chatFrame.isClosed()) {
						running = false;
						break;
					}

					input = "NO_INPUT";
					if (fromServer.ready()) {
						input = fromServer.readLine();
					}

					if (input.equals("NO_INPUT")) {
						/* do nothing */;
					} else if (input.startsWith("IPCT_SERVER:MSG/")) {
						String str = input.split("/", 2)[1];
						chatFrame.displayMessage(str);

					} else if (input.equals("IPCT_SERVER:CONNECTION_CLOSED")) {
						break MAIN_SERVER_IO;

					} else {
						toServer.println("IPCT_SERVER:ILLEGAL_INPUT");
						break MAIN_SERVER_IO;
					} 

					while (chatFrame.hasNextOutgoingMsg()) {
						String msg = chatFrame.getNextOutgoingMsg();
						toServer.println("IPCT_CLIENT:MSG/" + msg);
					}
					
				}
				
			} // end MAIN_SERVER_IO

			toServer.println("IPCT_CLIENT:LEAVING");
			System.exit(0);
			
		} catch (IOException ioe) {
			failGracefully(ioe);
			System.exit(1);
		}
	}

	private void failGracefully(Exception e) {
		System.err.println("IPCTClient: unable to connect to server. Stack trace:");
		e.printStackTrace();

		JOptionPane.showMessageDialog(null, "Couldn't connect to the server."+System.getProperty("line.separator")
		+"Next time, please make sure the server address is correct.");
	}
	
}
