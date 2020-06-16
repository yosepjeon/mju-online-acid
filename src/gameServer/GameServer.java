package gameServer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import protocolData.*;

public class GameServer extends Thread {
	private boolean flag = true;

	private Protocol data;

	private Socket socket;

	private ObjectInputStream in;

	private ObjectOutputStream out;

	protected String name;

	private ServerInterface server;

	private LobbyInterface lobby;

	private GameRoomInterface room;

	private int userLocation, roomNumber;

	private boolean isReady = false;
	private boolean isLogin = false;
	private boolean isMyTurn = false;

	public GameServer(Socket socket, LobbyInterface lobby) {
		System.out.println("Game Server New!!");
		LogFrame.print("Game Server New!!");

		setUserLocation(ServerInterface.LOBBY);

		this.lobby = lobby;
		this.name  = socket.getInetAddress().toString();
		setLobbyInstance();

		this.socket = socket;
		try {
			in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("-- ["+name+"]");
			lobby.subSocket(name);

            if( this.socket != null) {
	            try {
	                this.socket.close();
	            } catch (IOException e1) {
	                e1.printStackTrace();
	            }
            }
			return;
        }

		this.start();
	}

	public void run() {
		while (flag) {

			try {
				data = (Protocol) in.readObject();

				System.out.println("=====================================");
				LogFrame.print("=====================================");
				
				System.out.println("From Clinet : " + data);
				LogFrame.print("From Clinet : " + data);
				
			} catch (ClassNotFoundException e) {
				System.out.println("연결이 끊겼습니다.");

			} catch (EOFException e) {
				continue;

			} catch ( SocketException e ) {
				System.out.println(" 연결이 끊겻습니다.");
				flag = false;
				
				exitUser();
				continue;
			} catch (IOException e) {
				System.out.println(" 연결이 끊겻습니다.");
				break;

			} 

			if (data instanceof ChatData)
				analysisChatData((ChatData) data);

			else if (data instanceof LobbyData)
				analysisLobbyData((LobbyData) data);

			else if (data instanceof GameLobbyData)
				analysisGameLobbyData((GameLobbyData) data);

			else if (data instanceof GameData)
				analysisGameData((GameData) data);
			else if(data instanceof EndData)
				analysisEndData((EndData) data);
			else {
			}
		}

		System.out.println("Exit");

		lobby.printState();

	}

	protected void sendMessage(Protocol data) throws Exception {
		try {
			out.writeObject(data);
		} catch ( NullPointerException e ) {
			System.out.println("연결이 끊긴 사용자 입니다.");
			throw new Exception("사용자 연결이 끊겼습니다. NullPointerException!! ");
		} catch (IOException e) {
			System.out.println("연결이 끊긴 사용자 입니다.");
			System.exit(0);
			throw new Exception("사용자 연결이 끊겼습니다. " + e );
			
		}
	}

	protected void stopThisThread() {
		this.flag = false;
	}

	/*
	 * when First enter Game or exit GameRoom call!
	 */
	private void setRoomUserList() {
		sendUserList();
		sendRoomList();
	}
	
	private void analysisEndData(EndData data)
	{
			room.broadcasting(data);
	}
	
	private void analysisChatData(ChatData data) {

		switch (data.getProtocol()) {

		case ChatData.ENTER:
			/*
			 * Enter program
			 */
			// Version Check!!
			String version = data.getMessage(); // get Client Version..
			this.name = data.getName();
			
			if (!version.equals(""+MainServer.VERSION)) {
				analysisChatData(new ChatData(data.getName(), null, ChatData.LOGIN_CHECK));
			
			} else {
				setLogin(true);
				server.broadcasting(data);
				setRoomUserList();
			}
			
			break;

		case ChatData.LOGIN_CHECK:
			try {
				sendMessage(data);
			} catch (Exception e) {
				e.printStackTrace();
			}

		case ChatData.EXIT:
			exitUser( this.data );
			break;

		case ChatData.MESSAGE:
			/*
			 * send message
			 */
			server.broadcasting(data);
			break;

		case ChatData.MESSAGE_SLIP:
			/*
			 * send Slip message
			 */
			server.sendSlip(data);
			break;

		case ChatData.SEND_TOTAL_USER:
			// client request userlist.
			Vector<String> list = new Vector<String>();

			for (GameServer temp : server.getUserList().getCollection())
				list.add(temp.getUserName());

			data.setUserList(list);
			try {
				sendMessage(data);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		default:
			break;
		}

	}

	/**
	 * 강제 종료 상황에서 서버에서 유저를 끊을때,
	 */
	private void exitUser() {
		try {
		ChatData data = new ChatData(name, "exit", ChatData.EXIT );
		exitUser( data );
		} catch (Exception e) {
			System.exit(0);
		}
	}
	
	/**
	 * 유저가 접속을 끊음을 처리
	 * @param data
	 */
	private void exitUser( Protocol data ) {
		if(isLogin) {
			server.broadcasting(data);
		}
			server.subSocket(data.getName());
			sendUserList();

		if (getUserLocation() != ServerInterface.LOBBY)
			analysisGameLobbyData(new GameLobbyData(data.getName(), null,
					GameLobbyData.EXIT_ROOM));

		stopThisThread();
	}
	
	
	/*
	 * When client is First Connetion...
	 */
	protected void sendUserList() {
		System.out.println("In Method[sendUserList()]");
		LogFrame.print("In Method[sendUserList()]");
		
		ChatData data = new ChatData(null, null, ChatData.SEND_USER_LIST);
		data.setUserList(server.getStringUser());
		server.broadcasting(data);
	}

	protected void sendRoomList() {
		server.broadcasting(new LobbyData(lobby.getRoomListAsString(),
				LobbyData.SEND_ROOMLIST));
	}

	private void analysisLobbyData(LobbyData data) {
		switch (data.getProtocol()) {

		case LobbyData.CREATE_ROOM:
			/*
			 * create Room...
			 */
			setUserLocation(ServerInterface.IN_GAME_ROOMKING);

			this.room = new GameRoom(data.getRoomName(), lobby.getUserList());
			lobby.addRoom(room);
			roomNumber = room.getNumber();
			setReady(true); 

			setRoomUserList();

			setRoomInstance(room);

			data.setRoomNumber(roomNumber);
			try {
				sendMessage(data);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case LobbyData.ENTER_TO_ROOM:
			/*
			 * enter to room....
			 */
			room = lobby.getSelectedRoom(data.getRoomName());
			int userCount = room.getUserCounter();

			if (userCount < 3) {
				setUserLocation(ServerInterface.IN_GAME_CRHARANGER);

				sendUserList(); //  대기실 사용자 초기화.

				roomNumber = room.getNumber();
				setRoomInstance(room);

				data.setUserList(room.getStringUser());
				data.setRoomNumber(roomNumber);

				try {
					sendMessage(data);
				} catch (Exception e) {
					e.printStackTrace();
				}
				room.sendTo(ServerInterface.IN_GAME_ROOMKING, data);
				sendUserList();

			} else {
				room = null;

				try {
					sendMessage(new ChatData("알림", "해당 방은 들어갈수 없습니다.", ChatData.MESSAGE));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			break;

		case LobbyData.SEND_ROOMLIST:
			/*
			 * send Room List...
			 */
			break;

		case LobbyData.EXIT_GAME:
			/*
			 * exit Game;;;
			 */
			break;

		default:
			break;
		}

	}

	private void setRoomInstance(GameRoomInterface room) {
		this.room = room;
		this.server = room;
	}

	private void setLobbyInstance() {
		this.room = null;
		this.server = lobby;
	}

	private void analysisGameLobbyData(GameLobbyData data) {
		switch (data.getProtocol()) {

		case GameLobbyData.CANCEL_READY:
			/*
			 * ready is canceled...
			 */
			setReady(false);
			room.sendTo(ServerInterface.IN_GAME_ROOMKING, data);

			break;

		case GameLobbyData.EXIT_ROOM:
			/*
			 * exit the room...
			 */

			if (userLocation == ServerInterface.IN_GAME_ROOMKING) {
				lobby.subRoom(roomNumber);
				setUserLocation(ServerInterface.LOBBY);
				server.broadcasting(data);
				room.exitRoomMaster(data);

			} else {
				setUserLocation(ServerInterface.LOBBY);
			}

			setLobbyInstance();

			sendUserList();
			setRoomUserList();

			break;

		case GameLobbyData.GAME_READY:
			/*
			 * game is ready...
			 */
			setReady(true);

			if (room.isAllReady())
				room.sendTo(ServerInterface.IN_GAME_ROOMKING, data);

			break;

		case GameLobbyData.GAME_START:
			/*
			 * game start!!
			 */
			data.setMessage("Game Start!");
			data.setName("NOTICE");

			room.broadcasting(data);
			sendUserList();
			room.setStart(true);

			break;

		default:
			break;
		}
	}

	private void analysisGameData(GameData data) {
		switch (data.getProtocol()) {
		case GameData.EXIT_THEGAME:
			/*
			 * exit the game
			 */
			if (this.getUserLocation() == ServerInterface.IN_GAME_ROOMKING) {

				server.broadcasting(new GameData(name, true,
						GameData.EXIT_THEGAME));

				sendUserList();

			} else {
				server.broadcasting(new GameData(name, false,
						GameData.EXIT_THEGAME));
				sendUserList();
			}
			setUserLocation(ServerInterface.LOBBY);

			break;

		case GameData.REQUEST_RETURN:
			break;

		default:
			System.out.println("Error");
			break;
		}

	}

	public String toString() {
		return name;
	}

	public int getUserLocation() {
		return userLocation;
	}

	public String getUserName() {
		return name;
	}

	public void setUserLocation(int location) {
		this.userLocation = location;
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	private void setReady(boolean bool) {
		isReady = bool;
	}

	public boolean isReady() {
		return isReady;
	}

	public void toGameServer(Protocol data) {
		analysisGameLobbyData((GameLobbyData) data);
	}

	public boolean isLogin() {
		return isLogin;
	}
	
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	

}
