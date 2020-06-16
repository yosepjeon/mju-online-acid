package gameClient;


import gui.GameLobby;
import gui.GameLobbyInter;
import gui.GameRoomGui;
import gui.GuiInterface;
import gui.InfoFrame;
import gui.LobbyGui;
import gui.LobbyGuiInter;
import gui.LoginPanel;
import gui.MainFrame;
import gui.PanelInterface;
import gui.RoomGuiInter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JOptionPane;

import protocolData.*;

public class ClientLobby implements ClientInterface {

	private final static double VERSION = 1.01;
	
	private final String serverIP = "192.168.34.70"; 
	
	private String name;
	private String challengerName;
	private String challenger2Name;
	
	private GuiInterface m_Frame;

	private LobbyGuiInter m_lobby; 

	private GameLobbyInter m_gameLobby;

	private RoomGuiInter m_gameRoom;
	private InfoFrame infoFrame;
	private Socket socket;
	private int total_score;
	private EndData e;

	private ObjectOutputStream out;

	private ObjectInputStream netIn;

	private Protocol data;

	private String receiver;
	private GuiInterface tstop;

	private boolean isInRoom = false;
	private boolean isRoomKing;
	private boolean isLogin = false;

	public ClientLobby(String id) {
		
		m_Frame = new MainFrame(this);
		m_Frame.setPanel(new LoginPanel(this));
		
		try {
			while(!isLogin) Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			socket = new Socket(serverIP, 9999);
			out = new ObjectOutputStream(socket.getOutputStream());
			netIn = new ObjectInputStream(socket.getInputStream());

			m_lobby = new LobbyGui(this);
			m_Frame.setPanel(m_lobby);
			e = new EndData(name);
			total_score = GameData.total_score;
			sendMessage(VERSION + "", ChatData.ENTER);

			working();

		} catch (IOException e) {
			JOptionPane.showConfirmDialog(null, 
					"서버가 꺼져있습니다.", "Notice!", JOptionPane.DEFAULT_OPTION);
			System.exit(0);
			
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	public void working() {
		try {
			while (true) {
				data = (Protocol) netIn.readObject();
				System.out.println("From Server : " + data);

				if (data instanceof ChatData)
					analysisChatData((ChatData) data);

				else if (data instanceof LobbyData)
					analysisLobbyData((LobbyData) data);

				else if (data instanceof GameLobbyData)
					analysisGameLobbyData((GameLobbyData) data);

				else if (data instanceof GameData)
					analysisGameData((GameData) data);
				else if(data instanceof EndData){
					EndData.total_score2 = ((EndData) data).total_score;
					EndData.name2 = ((EndData) data).name;
					analysisEndData((EndData) data);}
				else {
					System.out.println("맞는 데이터형이 없음.");
				}

			}

		} catch (ClassNotFoundException e) {
			System.out.println("Class not found");
		} catch (IOException e) {
			System.out.println("data read fail");
			System.exit(0);
		}
	}

	private void analysisEndData(Protocol data)
	{
		String w;
		EndData e = (EndData)data;
	}
	
	private void analysisChatData(ChatData data) {
		switch (data.getProtocol()) {

		case ChatData.ENTER:
			data.setMessage("님이 입장하셨습니다.");
			setTextToLog(data);
			break;
			
		case ChatData.LOGIN_CHECK:
			JOptionPane.showConfirmDialog(null, 
					"", "Notice!", JOptionPane.DEFAULT_OPTION);
			System.exit(0);
			break;
			
		case ChatData.EXIT:
			setTextToLog(data);
			break;

		case ChatData.MESSAGE:
			setTextToLog(data);
			break;

		case ChatData.MESSAGE_SLIP:
			m_Frame.showMessageBox(data.getName(), data.getMessage(), false);
			break;

		case ChatData.SEND_USER_LIST:
			System.out.println("in send user list");
			m_Frame.setUserList(data.getUserList());
			break;

		case ChatData.SEND_TOTAL_USER:
			m_Frame.setTotalUser(data.getUserList());
			break;

		default:
			setTextToLog(data);
			break;
		}
	}

	private void analysisLobbyData(LobbyData data) {
		switch (data.getProtocol()) {

		case LobbyData.CREATE_ROOM:
			/*
			 * create Room...
			 */
			setRoomKing(true);
			
			changePanel(new GameLobby(this, true));
			setGameLobbyInstance();
			m_gameLobby.setRoomKing(name);

			m_gameLobby.setStartButton(true);

			m_gameLobby.setGameRoomInf("[" + data.getRoomNumber() + "번방] "
					+ data.getRoomName());

			break;

		case LobbyData.ENTER_TO_ROOM:
			changePanel(new GameLobby(this,false));
			setGameLobbyInstance();
			m_gameLobby.setChallenger(this.name);
			m_gameLobby.setChallenger(challengerName);
			m_gameLobby.setChallenger2(this.name);
			m_gameLobby.setChallenger2(challenger2Name);
			m_gameLobby.setStartButton(false);

			Vector<String> temp = new Vector<String>();

			for (String user : data.getUserList())
				temp.add(user);

			m_gameLobby.setUserList(temp);

			m_gameLobby.setGameRoomInf("[" + data.getRoomNumber() + "번방] "
					+ data.getRoomName());
			
			showEnterMessage(data);
			
			break;

		case LobbyData.SEND_ROOMLIST:
			/*
			 * send Room LIst...
			 */
			((RoomGuiInter) m_Frame).setRoomList(data.getRoomList());

			break;

		case LobbyData.EXIT_GAME:
			/*
			 * when Room king exit this room.
			 */
			break;

		default:
			break;
		}

	}

	private void showEnterMessage(Protocol data) {
		m_Frame.setTextToLogWindow("[ " + data.getName() + " ] 님이 입장하셨습니다. \n");
	}

	private void showExitMessage(Protocol data) {
		m_Frame.setTextToLogWindow("[ " + data.getName() + " ] 님이 나가셨습니다. \n");
	}

	private void analysisGameLobbyData(GameLobbyData data) {
		switch (data.getProtocol()) {

		case GameLobbyData.CANCEL_READY:
			/*
			 * ready is canceled...
			 */
			m_gameLobby.setButtonEnable(false);

			break;

		case GameLobbyData.EXIT_ROOM:
			/*
			 * exit the room...
			 */
			JOptionPane.showConfirmDialog(null, "Room Master exit this room!",
					"Notice!", JOptionPane.DEFAULT_OPTION);

			isInRoom = false;
			changePanel(new LobbyGui(this));
			setLobbyInstance();

			break;

		case GameLobbyData.GAME_READY:
			/*
			 * All gamer is ready... (only roomking execute...)
			 */
			m_gameLobby.setButtonEnable(true);

			break;

		case GameLobbyData.GAME_START:
			/*
			 * game start
			 */
			try {
				changePanel(new GameRoomGui(this));
			} catch (Exception e) {
			}
			setGameRoomGui();

			setTextToLog(data);
			
			m_gameRoom.setGameRoomInfo(m_gameLobby.getGameInfo());

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
			setRoomKing(false);
			System.out.println("##### gameData in");
				changePanel(new GameLobby(this, true));
				setGameLobbyInstance();
				m_gameLobby.setStartButton(false);
				JOptionPane.showConfirmDialog(null, "누군가 나갔습니다!",
						"Notice!", JOptionPane.DEFAULT_OPTION);
				GameData.ttt.stop();

			System.out.println("##### gameData out");
			break;

		case GameData.SEND_RESULT:
			/*
			 * send Victory...
			 */
			if(isRoomKing())
				sendMessage("", GameData.SEND_RESULT);			
			
			break;
			
		case GameData.REQUEST_RETURN:
			
			sendMessage("최종 점수", GameData.SEND_RESULT);
			
			break;

		case GameData.SEND_GAME_MESSAGE:
			infoFrame = new InfoFrame(data.getMessage());
			infoFrame.setVisible(true);
			
			break;

			
		default:
			break;
		}

	}

	private void setTextToLog(Protocol data) {
		m_Frame.setTextToLogWindow("[ " + data.getName() + " ] " + data.getMessage()
				+ "\n");
	}
	
	private void setTextToLog2(Protocol data,int i) {
		m_Frame.setTextToLogWindow("[ " + data.getName() + " ] " + i
				+ "\n");
	}
	
	private void setTextToLog(String str) {
		m_Frame.setTextToLogWindow("[ " + "알림" + " ] " + str
				+ "\n");
	}

	private void setGameRoomGui() {
		this.m_gameRoom = (RoomGuiInter) this.m_Frame;
	}

	private void setGameLobbyInstance() {
		this.m_gameLobby = (GameLobbyInter) this.m_Frame;
	}

	private void setLobbyInstance() {
		this.m_lobby = (LobbyGuiInter) this.m_Frame;
	}

	/*
	 * User send when All Messsage call
	 */
	public void sendMessage(String message, short state) {
		try {
			if (state == ChatData.MESSAGE_SLIP) {
				data = new ChatData(receiver, name, message,
						ChatData.MESSAGE_SLIP);
				out.writeObject(data);

			} else if (state == LobbyData.CREATE_ROOM) {
				// message is Room name.
				isInRoom = true;

				data = new LobbyData(name, message, state);
				out.writeObject(data);

			} else if (state == LobbyData.ENTER_TO_ROOM) {
				// message is Room name.
				isInRoom = true;

				data = new LobbyData(name, message, state);
				out.writeObject(data);

			} else if (state == GameLobbyData.EXIT_ROOM) {
				isInRoom = false;

				data = new GameLobbyData(name, message, state);
				data.setName(name);

				changePanel(new LobbyGui(this));
				setLobbyInstance();
				out.writeObject(data);

			} else if (state == GameLobbyData.GAME_START) {
				data = new GameLobbyData(name, message, state);
				out.writeObject(data);

			} else if (state == GameLobbyData.GAME_READY) {
				data = new GameLobbyData(name, message, state);
				out.writeObject(data);

			} else if (state == GameLobbyData.CANCEL_READY) {
				data = new GameLobbyData(name, message, state);
				out.writeObject(data);

			} else if (state == GameData.EXIT_THEGAME) {
		//		changePanel(new GameLobby(this, true));
				changePanel(new LobbyGui(this));
				setLobbyInstance();
		//		m_gameLobby.setStartButton(true);

		//		data = new GameData("", state);
				out.writeObject(data);

			} else if (state == ChatData.SEND_TOTAL_USER) {
				data = new ChatData(name, null, state);
				out.writeObject(data);
				
			} else if (state == GameData.REQUEST_RETURN) {
				data = new GameData("", state);
				out.writeObject(data);
				
			} else if (state == GameData.RESPONSE_RETURN) {
				data = new GameData(message, state);
				out.writeObject(data);
				
			} else if (state == GameData.SEND_RESULT) {
				data = new GameData(message,state);
				out.writeObject(data);
				
			}else if(state == EndData.SEND_RESULT)
			{
				data = new EndData(EndData.name,EndData.total_score);
				out.writeObject(data);
			} else {
				try {
					data = new ChatData(name, message, state);
					out.writeObject(data);
				} catch (Exception e) {
					System.exit(0);
				}
			}

		} catch (IOException e) {
			System.out.println("data write fail!");
		}
	}
	/*
	 * When Enter the Game Lobby, this method execute.
	 */
	public void sendSlip(String to, String message, short state) {
		receiver = to;
		sendMessage(message, state);
	}

	public GuiInterface getGui() {
		return m_Frame;
	}

	public void changeGui(GuiInterface gui) {
		m_Frame.unShow();
		m_Frame = gui;
	}
	
	public void changePanel(GuiInterface panel) {
		if (panel instanceof LoginPanel)
			m_Frame.setPanel((PanelInterface)panel);
		else if (panel instanceof LobbyGui)
			m_Frame.setPanel((LobbyGuiInter)panel);
		else if (panel instanceof GameLobby)
			m_Frame.setPanel((GameLobbyInter)panel);
		else if (panel instanceof GameRoomGui)
			m_Frame.setPanel((RoomGuiInter)panel);
		else {
			System.out.println("맞는 데이터형이 없음.");
		}
		tstop = panel;
	}

	public static void main(String[] args) {
		try {
			new ClientLobby("test");
		} catch (Exception e) {
		}
	}

	public void setSlipTo(String slipTo) {
		this.receiver = slipTo;
	}

	public boolean isRoomKing() {
		return isRoomKing;
	}
	

	public void setRoomKing(boolean isRoomKing) {
		this.isRoomKing = isRoomKing;
	}

	public void setName(String text) {
		this.name = text;
		isLogin = true;
	}
	

}
