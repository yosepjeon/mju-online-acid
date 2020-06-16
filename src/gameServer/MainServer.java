package gameServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
	
	private static final int PORT = 9999;
	
	public static final double VERSION = 1.01;
	private ServerSocket serverSocket;
	private LobbyInterface lobby;
	private boolean flag = true;
	
	
	public MainServer() throws IOException {
		serverSocket = new ServerSocket(PORT);
		lobby = new Lobby();
		Socket socket = new Socket();
		
		System.out.println("Main Server Start!");
		LogFrame.print("Main Server Start!");
		while (flag) {
			try {
				socket = serverSocket.accept();
				lobby.addGamer(new GameServer( socket, lobby));
					
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			} 
		}
		
		try{} catch( Exception e ) {} finally {
			socket.close();
		}
		
		serverSocket.close();
		System.out.println("Server End!!");
		LogFrame.print("Server End!!");
	}
	
	public static void main(String[] args) {
		new LogFrame();
		
		try {
			new MainServer(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class LogFrame { 

	
	public LogFrame() {
		
			
	}
	
	public static void print(String str) {

	}
}