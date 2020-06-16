package protocolData;

import gui.GuiInterface;

import java.util.Vector;

public class GameData implements Protocol {

	private static final long serialVersionUID = 1L;

	public static final short SEND_RESULT = 7200;
	public static final short SEND_GAME_MESSAGE = 7300;
	public static final short EXIT_THEGAME = 7999;
	public static final short REQUEST_RETURN = 8000;
	public static final short RESPONSE_RETURN = 8100;
	public static final short END_ACID = 10;
	public static Thread ttt;
	
	private String name;
	
	private boolean roomkingexit;
	private boolean is;
	private boolean isReturn;
	private short protocol;
	
	private GuiInterface m_Frame;
	
	public static int endGame=0;
	public static int total_score;
	public GameData(){}
	public GameData(int a,int b){endGame = a;total_score = b;}
	public GameData(Thread t){this.ttt=t;}
	public GameData(String name, boolean is, short protocol) {
		this.name = name;
		this.is = is;
		this.protocol = protocol;
	}
	
	public GameData(String isReturn, short protocol) {
		if (isReturn.equals("YES"))
			this.isReturn = true;
		else if (isReturn.equals("NO")) 
			this.isReturn = false;
		
		this.protocol = protocol;
	}

	public GameData(int[] location, short protocol) {
		this.protocol = protocol;
	}

	public short getProtocol() {
		return protocol;
	}

	public Vector<String> getUserList() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUserList(Vector<String> userList) {
		// TODO Auto-generated method stub
	}

	public void setMessage(String message) {
		// TODO Auto-generated method stub
	}
	
	public boolean isReturn() {
		return isReturn;
	}

}
