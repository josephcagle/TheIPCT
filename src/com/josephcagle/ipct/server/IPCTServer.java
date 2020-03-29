package com.josephcagle.ipct.server;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;

public class IPCTServer {
		
	private volatile boolean running = true;  // the stop trigger for 'graceful shutdown' pattern
	
	private final List<String> conversation = Collections.synchronizedList(new LinkedList<>());
	private final List<IPCTServer.ConnectionHandler> connections = new ArrayList<>(3);  // start with 3; only allocate more space if needed

	// outgoing relative to server
	private final List<String> outgoingMsgs = Collections.synchronizedList(new LinkedList<String>());
	
	private static final int PORT = 23_435;
	
	
	public static void main(String[] args) {
		
		IPCTServer server = new IPCTServer();
		EventQueue.invokeLater(() -> {
			
			JFrame f = new JFrame();
			JButton b = new JButton("Stop Server");
			
			b.addActionListener(e -> {
				b.setText("Stopping...");
				b.setEnabled(false);
				server.stop();
			});
			
			f.setLayout(new BorderLayout());
			f.add(b, BorderLayout.CENTER);
			
			f.addWindowListener(new WindowAdapter() {
				@Override public void windowClosing(WindowEvent e) { b.doClick(); }
			});
			
			f.setSize(300, 100);
			f.setTitle("IPCT Server");
			f.setLocationRelativeTo(null);

			f.setVisible(true);
			
		});
		
		try {
			synchronized (server) {
				server.wait();  // wait for server admin to click stop button
			}
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		
//		System.out.println("IPCT Server shutting down");
		try { Thread.sleep(1000); }  // give threads time to finish
		catch (InterruptedException e) { e.printStackTrace(); }
		System.exit(0); // then stop
		
	}

	
	public void stop() {
		this.broadcastMsg("IPCT SERVER: SHUTTING DOWN");
//		System.out.println("stop()");
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	public IPCTServer() {

		// this thread accepts connections and hands their i/o streams to the handler class
		Thread handoffThread = new Thread(() -> {
			
			while (running) {
				
				try  ( ServerSocket ss = new ServerSocket(PORT); ) {
					
					ss.setSoTimeout(300);
					Socket s = ss.accept(); // if timeout, caught by catch block and then ignored

					IPCTServer.ConnectionHandler handler = new IPCTServer.ConnectionHandler(s); 
					handler.start();
					connections.add(handler);
					
				} catch (SocketTimeoutException ignore) {
					// do nothing; setTimeout+accept->throw+catch is so loop keeps running (checking this.running) instead of blocking
				} catch (IOException ioe) {
//					System.out.println("exception in handoffThread");
					ioe.printStackTrace();
				}
			}
			
			while (!connections.isEmpty()) {
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
			
		});
		handoffThread.start();

		// sends messages to connections
		Thread sendMessages = new Thread(() -> {
			
			while (running) {
				
				if (!this.outgoingMsgs.isEmpty()) {
					for (IPCTServer.ConnectionHandler connection : connections) {
						if (connections.contains(connection)) { // check to make sure this connection didn't get closed
															   // while we were sending messages to other ones
							
							// make a temporary list so the original survives removing elements
							List<String> tmpList = new LinkedList<>(IPCTServer.this.outgoingMsgs);
							
							for (String msg : tmpList) {
								connection.sendMessage(msg);
								tmpList.remove(msg);
							}
						}
					}
					
					// all messages sent; so ...
					IPCTServer.this.outgoingMsgs.clear();
				}

				// don't use all the processor cycles
				try { Thread.sleep(80); } catch (InterruptedException ie) { ie.printStackTrace(); }

			}
		});
		sendMessages.start();
		
	} // end IPCTServer()

	
	private synchronized void broadcastMsg(String s) { // send message to all connections
		this.conversation.add(s);
		this.outgoingMsgs.add("IPCT_SERVER:MSG/" + s);
	}
	
	
	private class ConnectionHandler extends Thread { // handles i/o and processing, including Socket::close
		
		private String clientUsername;
		private Socket socket;
		private List<String> outgoingMsgs = new LinkedList<>();

		public ConnectionHandler(Socket s) {
			this.socket = s;
		}
		
/*
copy and paste this for client-side server input
-------
IPCT_CLIENT:CONNECTION_REQUEST
IPCT_CLIENT:USERNAME/<name>

IPCT_CLIENT:MSG/message
*/
		
		@Override
		public void run() {  // talk to client

			TRY_BLOCK: try ( PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				
				String input = fromClient.readLine();
				if (!input.equalsIgnoreCase("IPCT_CLIENT:CONNECTION_REQUEST")) {
					toClient.println("IPCT_SERVER:ILLEGAL_INPUT");
					toClient.println("IPCT_SERVER:CONNECTION_REFUSED");
					break TRY_BLOCK;
				}

				input = fromClient.readLine();
				clientUsername = input.split("/", 2)[1]; // should be "IPCT_CLIENT:USERNAME/<name>"

				toClient.println("IPCT_SERVER:CONNECTION_ACCEPTED");
				toClient.println("IPCT_SERVER:SENDING_CURRENT_CONVERSATION");
				toClient.println(IPCTServer.this.conversation.stream().collect(Collectors.joining(System.getProperty("line.separator"))));
				toClient.println("IPCT_SERVER:END_OF_CONVERSATION");

				IPCTServer.this.broadcastMsg(clientUsername + " has joined the chat.");

				
				while (running && (input != null)) {
					try {  // sleep 1/25th second
						Thread.sleep(80);  // don't waste processor time; people can't type as fast as a loop
					} catch (Exception e) { e.printStackTrace(); }

					
					input = "NO_INPUT";
					if (fromClient.ready()) { // don't block if there is no input
						input = fromClient.readLine();
					}

					if (input.equalsIgnoreCase("IPCT_CLIENT:LEAVING")) {
						break;
						
					} else if (input.startsWith("IPCT_CLIENT:MSG/")) { // should be "IPCT_CLIENT:MSG/message"; this means an actual message
						String msg = input.split("/", 2)[1];
						IPCTServer.this.broadcastMsg(this.clientUsername + ": " + msg);
						
					} else if (input.equals("NO_INPUT")) { // no input
						/* pass */;
						
					} else {
						toClient.println("IPCT_SERVER:ILLEGAL_INPUT");
						break;
					}

					
					this.outgoingMsgs.forEach(msg -> { // print all outgoing,						
						toClient.println(msg);
						this.outgoingMsgs.remove(msg); // and make sure they don't get printed next iteration
					});
					
				} // end while loop
				
				
				toClient.println("IPCT_SERVER:CONNECTION_CLOSED");
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				
				try {
					socket.close();					
					IPCTServer.this.connections.remove(this);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				IPCTServer.this.broadcastMsg(this.clientUsername + " has left the chat.");
				
			} // end of outer try block

		} // end run()

		public synchronized void sendMessage(String s) {
			this.outgoingMsgs.add(s);
		}

		
	} // end IPCTServer.ConnectionHandler

	
} // end IPCTServer
