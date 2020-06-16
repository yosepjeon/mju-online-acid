package protocolData;

import gui.GuiInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class EndData implements Protocol {
	public static final short SEND_RESULT = 11111;
	
	private static final long serialVersionUID = 1L;

	public static String name;
	public static String name2;
	String message=null;
	private short protocol;
	
	private GuiInterface m_Frame;
	private String realname;
	public static int endGame=0;
	public static int total_score;
	public static int total_score2;
	public static Vector<String> words;
	public static Vector<String> words_eng;
	public static ObjectInputStream in;
	public static ObjectOutputStream out;
	
	public EndData(){}
	public EndData(Vector<String> a){
//		try {
//		out.writeObject(a);
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
		//words.addAll(a);
		}

	public EndData(int[] location, short protocol) {
		this.protocol = protocol;
	}
	public EndData(String name, int score)
	{
		this.name = name;
		this.total_score = score;
	}
	public EndData(String name){this.name = name;}
	public EndData(Vector<String> words2, Vector<String> words_eng2) {
		words = words2;words_eng = words_eng2;
	}
	public EndData(int a){this.total_score = a;}
	public short getProtocol() {
		return protocol;
	}

	public Vector<String> getUserList() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name2;
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return message;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	public void setUserList(Vector<String> userList) {
		// TODO Auto-generated method stub
	}

	public void setMessage(String message) {
		// TODO Auto-generated method stub
		this.message=message;
	}
	
//	public int getScore() {
//		// TODO Auto-generated method stub
//		return total_score2;
//	}
	
}
