package gui;

import gameClient.ClientInterface;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import protocolData.ChatData;
import protocolData.EndData;
import protocolData.GameData;
import protocolData.LobbyData;

@SuppressWarnings("serial")
public class GameRoomGui extends JPanel implements RoomGuiInter, Runnable {
	private UserListFrame userListFrame;
	
	/*
	 *  Left Panel
	 */
	private JLabel m_titleLabel;
	private GameBoardCanvas m_board = new GameBoardCanvas();
	private JTextField m_input;
	private JPanel m_canvasPanel;

	/*
	 * Right Panel 
	 */
	private JPanel m_rightPanel;
	private JTextArea m_log;
	private JList m_userList;
	private JLabel m_gamer1,m_gamer2,m_gamer3;
	private ImageButton m_gamerCaptin, m_gamerChallenger, m_gamerChallenger2;
	private ImageButton m_exitButton, m_totalUserButton;
	private ImageButton m_scoreButton;
	private JScrollPane m_logScrollPan;
	private JScrollBar m_vScroll;
	
	private ClientInterface client;
	private EventExecute event;
	
	static final int WIDTH = 590;
	static final int HEIGHT = 593;
	final int max_life = 10; // 최대 생명 개수
	int index; // 단어를 맞췄을 때 해당 단어의 index를 저장할 변수
	int index_eng; // 영어 단어를 맞췄을 때 해당 영어 단어의 index를 저장할 변수
	int life; // 현재 보유 생명 
	int score; // 현재 점수 
	int total_score; // 남은 생명 수를 포함한 최종 점수 
	int level; // 플레이하는 레벨
	int earning_point; // 일반적인 단어를 맞첬을 때의 획득 점
	int point_for_hidden; // 아이템 1 - 9 의 개수를 늘려주는 숨겨진 단어를 맞췄을 때 획득 점수 
	int point_for_gold; // 5초간 화면에 보이다 사라지는 골드 단어를 맞췄을 때 획득 점수 
	int point_for_diamond; // 3초간 화면 이곳 저곳을 돌아다니는 다이아 단어를 맞췄을 때 획득 점수 
	int wrong_point; // 화면에 없는 단어를 입력했을 때 깎이는 점수 
	int missing_point; // 단어를 놓쳤을 때 깎이는 점수 
	int dont_type_point; // 입력하면 안되는 단어를 입력했을 때 깎이는 점수 
	int[] item = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }; // 각 아이템의 개수 
	int red; // R 색상값 
	int green; // G 색상값 
	int blue; // B 색상값 
	int ph; // pH 농도 
	int ph_line; // pH 농도 표시 라인  
	   
	Vector<String> words; // 데이터베이스에 저장된 한글 단어를 읽어와 저장할 배열 
	CopyOnWriteArrayList<Word> viewingWords; // 화면에 내려오는 한글 단어 
	Vector<String> words_eng; // 데이터베이스에 저장된 영어 단어를 읽어와 저장할 배열 
	CopyOnWriteArrayList<Word> viewingWords_eng; // 화면에 내려오는 영어 단어 
	BufferedReader inputStream;
	BufferedReader inputStream_eng;
	Thread t;
	long time;
	JTextField t1;
	   
	final long limit_time = 300; // 최대 플레이 가능 시간 (초)
	long play_time; // 쓰레드 시작부터 현재까지 지나온 시간 (0.1초)
	final long harder = play_time / 120; // 시간이 갈수록 난이도를 높여주는 변수 
	long item_two_duration; // 2번째 아이템의 지속 시간 
	long item_three_duration; // 3번째 아이템의 지속 시간 
	long item_five_duration; // 5번째 아이템의 지속 시간 
	long item_six_duration; // 6번째 아이템의 지속 시간 
	long item_seven_duration; // 7번째 아이템의 지속 시간 
	long item_eight_duration; // 8번째 아이템의 지속 시간 
	long item_ten_duration; // 10번째 아이템의 지속 시간 
	long wrong_duration; // 단어를 잘못 입력했을 때 화면에 0.5초간 잃은 점수를 표시할 변수 
	long miss_duration; // 단어를 놓쳤을 때 화면에 0.5초간 잃은 점수를 표시할 변수 
	long dont_type_duration; // 입력하면 안되는 단어를 맞췄을 때 0.5초간 잃은 점수를 표시할 변수 
	long blackout_duration; // 정전 시키는 단어 입력 시 정전 시간 설정할 변수 
	long get_duration; // 일반적인 단어를 맞췄을 때 화면에 0.5초간 획득 점수를 표시할 변수 
	long get_hidden_duration; // 아이템 획득 단어를 맞췄을 때 화면에 0.5초간 획득 점수를 표시할 변수 
	long gold_duration; // 골드 단어를 5초간 화면에 표시할 변수 
	long get_gold_duration; // 골드 단어를 맞췄을 때 화면에 1초간 획득 점수를 표시할 변수 
	long diamond_duration; // 다이아 단어를 3초간 화면에 표시할 변수 
	long get_diamond_duration; // 다이아 단어를 맞췄을 때 알림 문구를 1초간 표시할 변수 

	String[] donttype = { "괌", "책", "네트워크" }; // 입력하면 손해보는 단어 -> 네트워크는 정전 효과를 일으키는 단
	String[] hidden = { "사우디아라비아", "소프트웨어", "남아프리카", "스코틀랜드",
			"프로그래밍", "커뮤니케이션", "빗살수염벌레", "호모사피엔스", "아르헨티나" }; // 입력하면 아이템의 개수가 올라가는 단어 
	String gold = "데이터베이스"; // 골드 단어 -> 맞추지 못하면 화면에 나타난 일반 단어를 삭제해 사용자의 오타 유도 
	String diamond = "권위주의"; // 다이아 단어 -> 맞추지 못하면 화면에 나타난 일반 단어를 삭제해 사용자의 오타 유도 

	boolean wrong = false; // 내가 틀렸는지 유무를 저장할 변수 
	boolean miss = false; // 내가 놓쳤는지 유무를 저장할 변수 
	boolean dont_type = false; // 내가 입력하면 안되는 단어를 입력했는 지 유무를 저장할 변수 
	boolean blackout = false; // 정전시키는 단어를 입력했는지 유무를 저장할 변수 
	boolean get = false; // 내가 일반적인 단어를 맞췄는 지 유무를 저장할 변수 
	boolean get_hidden = false; // 내가 아이템 획득 단어를 입력했는지 저장할 변수 
	boolean active_item_two = false; // 2번째 아이템 사용 중인지 확인할 변수 
	boolean active_item_three = false; // 3번째 아이템 사용 중인지 확인할 변수 
	boolean active_item_five = false; // 5번째 아이템 사용 중인지 확인할 변수 
	boolean active_item_six = false; // 6번째 아이템 사용 중인지 확인할 변수 
	boolean active_item_seven = false; // 7번째 아이템 사용 중인지 확인할 변수 
	boolean active_item_eight = false; // 8번째 아이템 사용 중인지 확인할 변수 
	boolean active_item_ten = false; // 10번째 아이템 사용 중인지 확인할 변수 
	boolean show_gold = false; // 골드 단어가 화면에 나오고 있는 지 확인할 변수 
	boolean get_gold = false; // 골드 단어를 맞췄는 지 유무를 저장할 변수 
	boolean show_diamond = false; // 다이아 단어가 화면에 나오고 있는 지 확인할 변수 
	boolean get_diamond = false; // 다이아 단어를 맞췄는지 유무를 저장할 변수 

	Sound sound = new Sound(); // 여러 가지 음을 내기 위해 Sound 객체 선언 

	public GameRoomGui(ClientInterface client) throws IOException {
		// 각 항목들 초기화 
		level = 1; // 처음 시작 레벨 초기화 
		life = 10; // 처음 시작 생명 초기화 
		red = 152; // R 색상값 초기화 
		green = 185; // G 색상값 초기화 
		blue = 106; // B 색상값 초기화 
		ph = 7; // pH 농도 초기화 
		ph_line = 340; // pH 농도 표시 라인 초기화 
		score = 0; // 점수 초기화 
		total_score = 0; // 최종 점수 초기화 
		earning_point = 100 + 20 * level; // 일반 단어를 맞췄을 경우 획득하는 점수 초기화 
		point_for_hidden = 500 + 20 * level; // 아이템 획득 단어를 맞췄을 경우 획득하는 점수 초기화 
		point_for_gold = 1000 + 20 * level; // 골드 단어를 맞췄을 경우 획득하는 점수 초기화 
		point_for_diamond = 2000 + 20 * level; // 다이아 단어를 맞췄을 경우 획득하는 점수 초기화 
		wrong_point = 20; // 잘못 입력했을 경우 잃는 점수 초기화 
		missing_point = 40; // 단어를 놓쳤을 경우 잃는 점수 초기화 
		dont_type_point = 200; // 입력하면 안되는 단어 입력할 경우 잃는 점수 초기화 
	      
	    try // 데이터베이스에서 한글 단어를 읽어 들임 
		{
			inputStream = new BufferedReader(new FileReader("asset/data/words.txt"));
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		try // 데이터베이스에서 영어 단어를 읽어 들임 
		{
			inputStream_eng = new BufferedReader(new FileReader("asset/data/words_eng.txt"));
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	      
	     // 배열에 한글 단어 저장 
		words = new Vector<String>();
		viewingWords = new CopyOnWriteArrayList<Word>();
		String w;
		
		while((w = inputStream.readLine()) != null)
		{
			words.add(w);
		}
		
		// 배열에 영어 단어 저장 
		words_eng = new Vector<String>();
		viewingWords_eng = new CopyOnWriteArrayList<Word>();
		String w_e;
		
		while((w_e = inputStream_eng.readLine()) != null)
		{
			words_eng.add(w_e);
		}

	    t = new Thread(this);
	    setVisible(true);
	      
	    this.client = client;
	    this.event = new EventExecute(this, this.client);
	      
	    GameData g = new GameData(t);
	    execute();
	    t.start();
	}
	   
	   
	private class TextFieldListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{			
			// 아이템 8번 작동 중일 때 
			if(active_item_eight)
			{
				// 영어 단어를 먼저 확인 
				index_eng = -1;
				
				for(Word w_e: viewingWords_eng)
				{
					// 영어 단어 맞췄을 때 점수를 획득  
					if(t1.getText().equals(w_e.str_e))
					{
						sound.Correct();
						index_eng = viewingWords_eng.indexOf(w_e);
						score += earning_point;
						get_duration = play_time;
						get = true;
						t1.setText("");
						viewingWords_eng.remove(index_eng);
					}
				}
				
				// 화면에 있는 영어 단어를 입력하지 않은 경우 
				if(index_eng == -1)
				{
					// 아이템을 사용한 것은 아닌지 확인 
					if(t1.getText().equals("1"))
					{
						if((life < max_life) && (item[0] > 0)) // 생명이 꽉 차있을 때는 사용이 안됨 
						{
							sound.Item();
							life++;
							item[0]--; // 아이템을 사용하면 아이템 개수는 하나가 줄어듬 
						}
					}
					else if(t1.getText().equals("2"))
					{
						if(item[1] > 0)
						{
							sound.Item();
							item_two_duration = play_time; // 아이템을 사용한 그 순간을 저장 -> (play_time - item_N_duration) / 10 이면 해당 아이템 지속 시간이 나옴 
							active_item_two = true; // 2번 아이템을 사용함을 알림 
							earning_point *= 2; // 각 획득 점수 2배씩 
							point_for_hidden *= 2;
							point_for_gold *= 2;
							point_for_diamond *= 2;
							item[1]--;
						}
					}
					else if(t1.getText().equals("3"))
					{
						if(item[2] > 0)
						{
							sound.Item();
							item_three_duration = play_time;
							active_item_three = true;
							item[2]--;
						}
					}
					else if(t1.getText().equals("4"))
					{
						if(item[3] > 0)
						{
							sound.Item();
							score += 500;
							item[3]--;
						}
					}
					else if(t1.getText().equals("5"))
					{
						if(item[4] > 0)
						{
							sound.Item();
							item_five_duration = play_time;
							active_item_five = true;
							item[4]--;
						}
					}
					else if(t1.getText().equals("6"))
					{
						if(item[5] > 0)
						{
							sound.Item();
							item_six_duration = play_time;
							active_item_six = true;
							item[5]--;
						}
					}
					else if(t1.getText().equals("7"))
					{
						if(item[6] > 0)
						{
							sound.Item();
							item_seven_duration = play_time;
							active_item_seven = true;
							item[6]--;
						}
					}
					else if(t1.getText().equals("8"))
					{
						if(item[7] > 0)
						{
							sound.Item();
							item_eight_duration = play_time;
							active_item_eight = true;
							item[7]--;
						}
					}
					else if(t1.getText().equals("9"))
					{
						if(item[8] > 0)
						{
							sound.Item();
							viewingWords.clear();
							viewingWords_eng.clear();
							item[8]--;
						}
					}
					else if(t1.getText().equals("0"))
					{
						if(item[9] > 0)
						{
							sound.Item();
							item_ten_duration = play_time;
							active_item_ten =true;
							item[9]--;
						}
					}
				}
				
				// 한글 단어 확인 
				index = -1;
				
				for(Word w: viewingWords)
				{
					if(t1.getText().equals(w.str))
					{
						index = viewingWords.indexOf(w);
						
						// 입력하면 안되는 단어 입력 시 
						if(w.str.equals(donttype[0]) || w.str.equals(donttype[1]))
						{
							sound.Wrong();
							dont_type_duration = play_time;
							dont_type = true;
							score -= dont_type_point; // 점수를 잃음 
							life -= 2; // 생명 2개를 잃음 
						}
						// 정전시키는 단어 입력 시 
						else if(w.str.equals(donttype[2]))
						{
							blackout_duration = play_time; // 점수를 잃지도 얻지도 않음 
							blackout = true;
						}
						// 아이템 획득 단어 입력 시 
						else if(w.str.equals(hidden[0]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[0]++; // 해당 아이템 개수 증가 
						}
						else if(w.str.equals(hidden[1]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[1]++;
						}
						else if(w.str.equals(hidden[2]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[2]++;
						}
						else if(w.str.equals(hidden[3]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[3]++;
						}
						else if(w.str.equals(hidden[4]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[4]++;
						}
						else if(w.str.equals(hidden[5]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[5]++;
						}
						else if(w.str.equals(hidden[6]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[6]++;
						}
						else if(w.str.equals(hidden[7]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[7]++;
						}
						else if(w.str.equals(hidden[8]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[8]++;
						}
						else if(w.str.equals(gold))
						{
							sound.Gold_or_Diamond();
							get_gold_duration = play_time; // 골드 단어를 맞춘 바로 그 시간을 저장 
							get_gold = true; // 골드 단어 맞췄음을 알림 
							score += point_for_gold;
							show_gold = false; // 더 이상 골드 단어를 화면에 띄우지 않음 
							
							if(life < max_life) // 생명 하나 얻음 
							{
								life++;
							}
							else
							{
								item[0]++;
							}
						}
						else if(w.str.equals(diamond)) // 골드 단어와 같은 맥락 
						{
							sound.Gold_or_Diamond();
							get_diamond_duration = play_time;
							get_diamond = true;
							score += point_for_diamond;
							show_diamond = false;
							
							if(life < max_life)
							{
								if(life <= (max_life - 2))
								{
									life += 2;
								}
								else
								{
									life++;
									item[0]++;
								}
							}
							else
							{
								item[0] += 2;
							}
						}
						// 단어는 맞췄으나 그 단어가 일반 단어일 경우 점수만 증가 
						else if(!wrong && !miss)
						{
							sound.Correct();
							score += earning_point;
							get_duration = play_time;
							get = true;
						}
						
						t1.setText("");
						viewingWords.remove(index); // 단어를 맞췄으니 해당 단어를 삭제 
					}
				}
				
				// 입력한 단어가 화면에 있는 단어가 아닌 경우 
				if(index == -1)
				{
					// 아이템을 사용한 것인지 확인 
					if(t1.getText().equals("1"))
					{
						if((life < max_life) && (item[0] > 0))
						{
							sound.Item();
							life++;
							item[0]--;
						}
					}
					else if(t1.getText().equals("2"))
					{
						if(item[1] > 0)
						{
							sound.Item();
							item_two_duration = play_time;
							active_item_two = true;
							earning_point *= 2;
							point_for_hidden *= 2;
							point_for_gold *= 2;
							item[1]--;
						}
					}
					else if(t1.getText().equals("3"))
					{
						if(item[2] > 0)
						{
							sound.Item();
							item_three_duration = play_time;
							active_item_three = true;
							item[2]--;
						}
					}
					else if(t1.getText().equals("4"))
					{
						if(item[3] > 0)
						{
							sound.Item();
							score += 500;
							item[3]--;
						}
					}
					else if(t1.getText().equals("5"))
					{
						if(item[4] > 0)
						{
							sound.Item();
							item_five_duration = play_time;
							active_item_five = true;
							item[4]--;
						}
					}
					else if(t1.getText().equals("6"))
					{
						if(item[5] > 0)
						{
							sound.Item();
							item_six_duration = play_time;
							active_item_six = true;
							item[5]--;
						}
					}
					else if(t1.getText().equals("7"))
					{
						if(item[6] > 0)
						{
							sound.Item();
							item_seven_duration = play_time;
							active_item_seven = true;
							item[6]--;
						}
					}
					else if(t1.getText().equals("8"))
					{
						if(item[7] > 0)
						{
							sound.Item();
							item_eight_duration = play_time;
							active_item_eight = true;
							item[7]--;
						}
					}
					else if(t1.getText().equals("9"))
					{
						if(item[8] > 0)
						{
							sound.Item();
							viewingWords.clear();
							viewingWords_eng.clear();
							item[8]--;
						}
					}
					else if(t1.getText().equals("0"))
					{
						if(item[9] > 0)
						{
							sound.Item();
							item_ten_duration = play_time;
							active_item_ten =true;
							item[9]--;
						}
					}
					else if(active_item_three)
					{ } // 아이템 3의 경우 무적이므로 점수를 잃지 않음 
					else if(index_eng == -1) // 입력한 단어가 영어 단어도 아니고 한글 단어도 아닌 경우에만 점수를 잃음 -> 그냥 else로 표현 시 영어 단어를 맞게 입력해도 점수를 얻음과 동시에 잃음 
					{
						sound.Wrong();
						wrong_duration = play_time;
						wrong = true;
						score -= wrong_point;
					}
				}
				
				t1.setText("");
				repaint();
			}
			// 아이템 8번 사용 중이 아닌 일반 적인 경우 로직은 완전히 동일 
			else
			{
				index = -1;
				
				for(Word w: viewingWords)
				{
					if(t1.getText().equals(w.str))
					{
						index = viewingWords.indexOf(w);
						
						if(w.str.equals(donttype[0]) || w.str.equals(donttype[1]))
						{
							sound.Wrong();
							dont_type_duration = play_time;
							dont_type = true;
							score -= dont_type_point;
							life -= 2;
						}
						else if(w.str.equals(donttype[2]))
						{
							blackout_duration = play_time;
							blackout = true;
						}
						else if(w.str.equals(hidden[0]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[0]++;
						}
						else if(w.str.equals(hidden[1]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[1]++;
						}
						else if(w.str.equals(hidden[2]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[2]++;
						}
						else if(w.str.equals(hidden[3]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[3]++;
						}
						else if(w.str.equals(hidden[4]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[4]++;
						}
						else if(w.str.equals(hidden[5]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[5]++;
						}
						else if(w.str.equals(hidden[6]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[6]++;
						}
						else if(w.str.equals(hidden[7]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[7]++;
						}
						else if(w.str.equals(hidden[8]))
						{
							sound.Hidden();
							get_hidden_duration = play_time;
							get_hidden = true;
							score += point_for_hidden;
							item[8]++;
						}
						else if(w.str.equals(gold))
						{
							sound.Gold_or_Diamond();
							get_gold_duration = play_time;
							get_gold = true;
							score += point_for_gold;
							show_gold = false;
							
							if(life < max_life)
							{
								life++;
							}
							else
							{
								item[0]++;
							}
						}
						else if(w.str.equals(diamond))
						{
							sound.Gold_or_Diamond();
							get_diamond_duration = play_time;
							get_diamond = true;
							score += point_for_diamond;
							show_diamond = false;
							
							if(life < max_life)
							{
								if(life <= (max_life - 2))
								{
									life += 2;
								}
								else
								{
									life++;
									item[0]++;
								}
							}
							else
							{
								item[0] += 2;
							}
						}
						else if(!wrong && !miss)
						{
							sound.Correct();
							score += earning_point;
							get_duration = play_time;
							get = true;
						}
						
						t1.setText("");
						viewingWords.remove(index);
					}
				}
				
				if(index == -1)
				{
					if(t1.getText().equals("1"))
					{
						if((life < max_life) && (item[0] > 0))
						{
							sound.Item();
							life++;
							item[0]--;
						}
					}
					else if(t1.getText().equals("2"))
					{
						if(item[1] > 0)
						{
							sound.Item();
							item_two_duration = play_time;
							active_item_two = true;
							earning_point *= 2;
							point_for_hidden *= 2;
							point_for_gold *= 2;
							point_for_diamond *= 2;
							item[1]--;
						}
					}
					else if(t1.getText().equals("3"))
					{
						if(item[2] > 0)
						{
							sound.Item();
							item_three_duration = play_time;
							active_item_three = true;
							item[2]--;
						}
					}
					else if(t1.getText().equals("4"))
					{
						if(item[3] > 0)
						{
							sound.Item();
							score += 500;
							item[3]--;
						}
					}
					else if(t1.getText().equals("5"))
					{
						if(item[4] > 0)
						{
							sound.Item();
							item_five_duration = play_time;
							active_item_five = true;
							item[4]--;
						}
					}
					else if(t1.getText().equals("6"))
					{
						if(item[5] > 0)
						{
							sound.Item();
							item_six_duration = play_time;
							active_item_six = true;
							item[5]--;
						}
					}
					else if(t1.getText().equals("7"))
					{
						if(item[6] > 0)
						{
							sound.Item();
							item_seven_duration = play_time;
							active_item_seven = true;
							item[6]--;
						}
					}
					else if(t1.getText().equals("8"))
					{
						if(item[7] > 0)
						{
							sound.Item();
							item_eight_duration = play_time;
							active_item_eight = true;
							item[7]--;
						}
					}
					else if(t1.getText().equals("9"))
					{
						if(item[8] > 0)
						{
							sound.Item();
							viewingWords.clear();
							item[8]--;
						}
					}
					else if(t1.getText().equals("0"))
					{
						if(item[9] > 0)
						{
							sound.Item();
							item_ten_duration = play_time;
							active_item_ten =true;
							item[9]--;
						}
					}
					else if(active_item_three)
					{ }
					else
					{
						sound.Wrong();
						wrong_duration = play_time;
						wrong = true;
						score -= wrong_point;
					}
				}
				
				t1.setText("");
				repaint(); // 로직 계산이 완료되면 화면을 다시 그림 
			}
		}
	} // TextFieldListener()

	 // 게임 플레이 쓰레드 
	public void run()
	{
		sound.BackGroundMusic();
		while(true)
		{
			try
			{
				t.sleep(100); // 0.1초간 대기 -> 쓰레드 한 바퀴 도는 데 0.1초 소요하도록 설정 
			} catch(InterruptedException ie)
			{
				ie.printStackTrace();
			}
			
			play_time++; // 플레이 시간은 0.1초 단위로 올라감 
			
			for(Word w: viewingWords)
			{
				// 아이템 획득 단어는 내려오는 속도를 매번 임의의 값으로 설정  
				if(w.str.equals(hidden[0]) || w.str.equals(hidden[1]) || w.str.equals(hidden[2])
					|| w.str.equals(hidden[3]) || w.str.equals(hidden[4]) || w.str.equals(hidden[5])
					|| w.str.equals(hidden[6]) || w.str.equals(hidden[7]) || w.str.equals(hidden[8]))
				{
					w.y += (int)(Math.random()) * 50 + ((int)(Math.random() * (5 + (level * 2) + harder))) * 5;
				}
				else if(active_item_six && (!w.str.equals(gold) && !w.str.equals(diamond)))
				{ } // 아이템 6번은 화면 정지이므로 아이템 획득 단어, 골드, 다이아 단어를 제외한 모든 단어는 일시 정지 
				else if(active_item_seven && (!w.str.equals(gold) && !w.str.equals(diamond))) // 아이템 7번은 슬로 모션이므로 아이템 획득 단어, 골드, 다이아 단어를 제외한 모든 단어는 매우 느리게 내려옴
				{
					w.y += 1;
				}
				else if(w.str.equals(gold)) // 골드 단어의 경우 
				{
					if(show_gold) // 이미 화면에 보여지고 있다면 아래 문장 무시 -> 무시하지 않으면 골드 단어가 나오는 순간이 계속 최신화 되어 단어를 입력할 때까지 무한히 화면에 보이게 됨 
					{
						continue;
					}
					
					gold_duration = play_time;
					show_gold = true;
					w.y = 100 + (int)(Math.random() * 600); // 임의의 위치에서 움직이지 않고 출력되도록 설정 
				}
				else if(w.str.equals(diamond)) // 다이아 단어의 경우 
				{
					// 위치를 이곳저곳 임의의 위치로 설정 
					w.x = 200 + (int)(Math.random() * 100);
					w.y = 100 + (int)(Math.random() * 100);
					
					if(show_diamond) // 골드 단어와 같은 맥락 
					{
						continue;
					}
					
					diamond_duration = play_time;
					show_diamond = true;
				}
				else // 일반 단어의 경우 레벨에 따라, 플레이한 시간에 따라 속도를 다르게 지정 (플레이한 시간이 길수록 빨라짐)
				{
					w.y += (5 + (level * 2) + harder);
				}
			}
			
			// 화면에 단어가 나오는 경우 
			if(!viewingWords.isEmpty())
			{
				// y값이 일정 위치를 지나버릴 때 
				if(viewingWords.get(0).y > HEIGHT - 50)
				{
					// 입력하면 안되는 단어나 아이템 획득 단어의 경우 놓쳐도 점수/생명을 잃지 않음 
					if(viewingWords.get(0).str.equals(donttype[0]) || viewingWords.get(0).str.equals(donttype[1]) || viewingWords.get(0).str.equals(donttype[2])
						|| viewingWords.get(0).str.equals(hidden[0]) || viewingWords.get(0).str.equals(hidden[1]) || viewingWords.get(0).str.equals(hidden[2])
						|| viewingWords.get(0).str.equals(hidden[3]) || viewingWords.get(0).str.equals(hidden[4]) || viewingWords.get(0).str.equals(hidden[5])
						|| viewingWords.get(0).str.equals(hidden[6]) || viewingWords.get(0).str.equals(hidden[7]) || viewingWords.get(0).str.equals(hidden[8]))
					{
						viewingWords.remove(0);
					}
					else if(active_item_three)
					{
						viewingWords.remove(0);
					} // 아이템 3번 사용 시에도 점수/생명을 잃지 않음 
					else // 일반 단어를 놓친 경우 생명을 하나 잃고 일정 점수를 잃고 해당 단어를 삭제 
					{
						sound.Miss();
						miss = true;
						miss_duration = play_time;
						life--;
						score -= missing_point;
						viewingWords.remove(0);
					}
				}
			}
			
			// 아이템 2번 사용 시 
			if(active_item_two)
			{
				if((play_time - item_two_duration) == 150) // 15초 동안 지속했으면 
				{
					// 2배이던 점수 원래대로 설정 
					earning_point /= 2;
					point_for_hidden /= 2;
					point_for_gold /= 2;
					point_for_diamond /= 2;
					active_item_two = false; // 아이템 2번 사용이 끝났음을 알림 
				}
			}
			
			// 아이템 3번 사용 시 
			if(active_item_three)
			{
				if((play_time - item_three_duration) == 100) // 10초 동안 지속했으면
				{
					active_item_three = false; // 아이템 3번 사용이 끝났음을 알림 
				}
			}
			
			// 아이템 5번 사용 시 
			if(active_item_five)
			{
				score += 10; // 0.1초마다 10점씩 총 1000점 획득 
				
				if((play_time - item_five_duration) == 100) // 10초 동안 지속했으면 
				{
					active_item_five = false; // 아이템 5번 사용이 끝났음을 알림 
				}
			}
			
			// 아이템 6번 사용 시 
			if(active_item_six)
			{
				if((play_time - item_six_duration) == 50) // 5초 동안 지속했으면 
				{
					active_item_six = false; // 아이템 6번 사용이 끝났음을 알림 
				}
			}
			
			// 아이템 7번 사용 시 
			if(active_item_seven)
			{
				if((play_time - item_seven_duration) == 100) // 10초 동안 지속했으면 
				{
					active_item_seven = false; // 아이템 7번 사용이 끝났음을 알림 
				}
			}
			
			// 아이템 8번 사용 시 
			if(active_item_eight)
			{
				// 영어 단어도 화면에서 내려옴 
				for(Word w_e: viewingWords_eng)
				{
					if(active_item_six)
					{ } // 영어 단어도 아이템 6번 사용 시 일시 정지 
					else if(active_item_seven) // 영어 단어도 아이템 7번 사용 시 느리게 내려옴 
					{
						w_e.y += 1;
					}
					else // 일반적인 경우 레벨에 따라 내려오는 속도가 다름 
					{
						w_e.y += (level + 3);
					}
				}
				
				// 영어 단어는 일정 위치에 도달하면 점수/생명을 잃지 않음 
				if(!viewingWords_eng.isEmpty())
				{
					if(viewingWords_eng.get(0).y > HEIGHT - 50)
					{
						viewingWords_eng.remove(0);
					}
				}
				
				// 아이템 6번 사용 중이 아니면 (화면이 멈춰 있는 경우 단어 생성을 막아 단어가 겹쳐져 나오지 않도록 설정)
				if(!active_item_six)
				{
					if((play_time % (67 - level - harder)) == 0) // 대략 6초에 하나씩 영어 단어가 생성됨
					{
						viewingWords_eng.add(new Word());
					}
				}
				
				if((play_time - item_eight_duration) == 400) // 40초 동안 지속했으면 
				{
					viewingWords_eng.clear(); // 영어 단어들을 모두 제거하고 
					active_item_eight = false; // 아이템 8번 사용이 끝났음을 알림 
				}
			}
			
			// 아이템 10번 사용 시 
			if(active_item_ten)
			{
				if((play_time - item_ten_duration) == 100) // 10초 동안 지속했으면 
				{
					active_item_ten = false; // 아이템 10번 사용이 끝났음을 알림 
				}
			}
			
			// 골드 단어를 맞췄을 경우 
			if(get_gold)
			{
				if((play_time - get_gold_duration) == 10) // 1초 동안 화면에 출력하고 
				{
					get_gold = false; // 화면에서 사라지도록 설정 
				}
			}
			
			// 다이아 단어를 맞췄을 경우 
			if(get_diamond)
			{
				if((play_time - get_diamond_duration) == 10) // 골드 단어와 같은 맥락 
				{
					get_diamond = false;
				}
			}
			
			// 틀렸을 경우 
			if(wrong)
			{
				if((play_time - wrong_duration) == 5) // 0.5초간 화면에 출력하고
				{
					wrong = false; // 화면에서 사라지도록 설정 
				}
			}
			
			// 놓치는 경우도 같은 맥락 
			if(miss)
			{
				if((play_time - miss_duration) == 5)
				{
					miss = false;
				}
			}
			
			// 입력하면 안되는 단어를 입력한 경우도 같은 맥락 
			if(dont_type)
			{
				if((play_time - dont_type_duration) == 5)
				{
					dont_type = false;
				}
			}
			
			// 일반 단어를 맞춘 경우도 같은 맥락 
			if(get)
			{
				if((play_time - get_duration) == 5)
				{
					get = false;
				}
			}
			
			// 아이템 획득 단어를 맞춘 경우도 같은 맥락 
			if(get_hidden)
			{
				if((play_time - get_hidden_duration) == 5)
				{
					get_hidden = false;
				}
			}
			
			// 골드 단어를 화면에 출력하는 시간 설정 
			if(show_gold)
			{
				if((play_time - gold_duration) == 50) // 5초간 화면에 출력하고 
				{
					show_gold = false; // 화면에 골드 단어가 출력되지 않도록 설정 
					viewingWords.remove(0); // 골드 단어는 맞추지 못하면 화면에 나타난 첫번째 단어부터 삭제해 사용자의 오타를 유도 
				}
			}
			
			// 다이아 단어를 화면에 출력하는 시간 설정 
			if(show_diamond)
			{
				 if((play_time - diamond_duration) == 30) // 3초간 화면에 출력 
				{
					show_diamond = false;
					viewingWords.remove(0); // 다이아 단어 역시 맞추지 못하면 화면에 나타난 첫번째 단어부터 삭제해 사용자의 오타를 유도 
				}
			}
			
			// 정전 단어 입력하면 3초간 글자가 화면에 보이지 않음 
			if(blackout)
			{
				if((play_time - blackout_duration) == 30)
				{
					blackout = false;
				}
			}
			
			// 시간에 따라 변하는 pH값에 맞는 위치에 현재 pH값 출력 및 잃는 점수 증가 
			if(play_time % 430 == 0)
			{
				ph--;
				ph_line += 30;
				wrong_point+= 20;
				missing_point += 40;
			}
						
			// 1분마다 레벨 상승 및 그에 따른 얻는 점수 증가 
			if(play_time % 600 == 0 && level < 5)
			{
				sound.Level_up();
				level++;
				earning_point = 100 + 20 * level;
				point_for_hidden = 500 + 20 * level;
				point_for_gold = 1000 + 20 * level;
				point_for_diamond = 2000 + 20 * level;
			}
			
			// 게임이 4분 진행되었을 때 
			if(play_time == 2400)
			{
				// 얻는 점수 모두 1.5배 
				earning_point *= 1.5;
				point_for_hidden *= 1.5;
				point_for_gold *= 1.5;
				point_for_diamond *= 1.5;
			}
			
			// 생명을 모두 일거나 플레이 시간이 5분이 되었을 때 현재까지 플레이 내역 출력 후 종료 
			if((life <= 0) || (play_time == (limit_time * 10)))
			{
				total_score = score + (life + item[0]) * level * 20;				
				viewingWords.clear();
				if(active_item_eight)
				{
					viewingWords_eng.clear();
				}
				repaint();
				JOptionPane.showMessageDialog(this, "게임 종료\n게임 시간: " +play_time / 10 +"초\n획득 점수: " +score 
						+"점\n남은 생명: " +life +"개 (+"+item[0] +")\n최종 점수: " +total_score +"점");
				LobbyData.total_score = total_score;
				GameData.total_score = total_score;
				EndData ggg = new EndData(total_score);
				EndData.total_score = total_score;
				EndData.total_score2 = total_score;
				EndData.name2 = EndData.name;
				client.sendMessage(String.valueOf(total_score),  ChatData.MESSAGE);
				t.stop();
			}
			
			// 아이템 6번이 사용 중이 아닌 경우 -> 화면이 정지되었을 때도 단어가 생성되면 단어들이 겹쳐질 가능성이 생기므로 
			if(!active_item_six)
			{
				// 대략 3초/6초/8 두 개의 루틴으로 단어 생성 -> 레벨에 높아질수록, 게임을 오래 진행할 수록 더 자주 단어 생성 
				if((play_time % (31 - level - harder) == 0) || (play_time % (59 - level - harder) == 0) || (play_time % (77 - level - harder) == 0))
				{
					viewingWords.add(new Word());
				}
			}
			
			repaint();
		}
	} // run()

	 public String remainingLife(int life)
	{
		StringBuilder sb = new StringBuilder(life);
		
		for(int i = 0; i < life; i++)
		{
			sb.append("♥");
		}
		
		return sb.toString();
	} // remainingLife()
	   
	// 잃은 생명의 수를 ♡로 표시하는 메소드 
	public String lostLife(int life)
	{
		StringBuilder sb = new StringBuilder(life);
		
		for(int i = 0; i < life; i++)
		{
			sb.append("♡");
		}
		
		return sb.toString();
	} // lostLife()
	   
	public void paint(Graphics g) {
		ImageIcon icon = new ImageIcon(
				URLGetter.getResource("image/gameRoomBg.png"));
		g.drawImage(icon.getImage(),0,0,null,null);
		this.paintComponents(g);	
	}
	
	private void execute() {
		addCanvas();
		addRightPanel();
	}

	private void addCanvas() {
		m_canvasPanel = new JPanel() {
			public void paint(Graphics g) {
				this.paintComponents(g);
			}
		};
		
		m_canvasPanel.add(m_board);
		m_canvasPanel.setLayout(null);
		m_board.setBounds(5, 5, 590, 593);
		
		add(m_canvasPanel);
		m_canvasPanel.setBounds(5,5,600,700);
		
		setLayout(null);
		
		
	}
	
	private void addRightPanel() {
		// userList don't add.
		m_rightPanel = new JPanel() {
			public void paint(Graphics g) {
				this.paintComponents(g);
			}
		};
		
		m_titleLabel = new JLabel("::: Game Infomation.");
		m_rightPanel.add(m_titleLabel);
		m_titleLabel.setBounds(5,10,200, 25);
		
		m_log = new JTextArea(4,50);
		m_gamer1 = new JLabel();
		m_rightPanel.add(m_gamer1);
		m_gamer1.setBounds(5,220,150,25);
		m_gamer2 = new JLabel();
		m_rightPanel.add(m_gamer2);
		m_gamer2.setBounds(145,220,150,25);
		m_gamer3 = new JLabel();
		m_rightPanel.add(m_gamer3);
		m_gamer3.setBounds(285,220,150,25);
		m_gamerCaptin = new ImageButton("image/user1.png", "user1");
		m_gamerChallenger = new ImageButton("image/user2.png", "user2");
		m_gamerChallenger2 = new ImageButton("image/user3.png", "user3");
		m_exitButton = new ImageButton("image/exitButton.jpg", "EXIT GAME", "image/exitButtonOver.jpg");
		
		// 상태창
		m_logScrollPan = new JScrollPane(m_log, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_rightPanel.add(m_logScrollPan);
		m_logScrollPan.setBounds(3,280,320,195);
		m_log.setEditable(false);
		m_log.setLineWrap(true);
		m_log.setText("");
		
		m_vScroll = m_logScrollPan.getVerticalScrollBar();
		m_vScroll.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if(!e.getValueIsAdjusting()) 
					m_vScroll.setValue(m_vScroll.getMaximum());
				
			}
		});

		
		m_input = new JTextField(10);
		m_rightPanel.add(m_input);
		m_input.setBounds(4, 482, 320, 30);
		m_input.addActionListener(event);
		
		
		
		m_totalUserButton = new ImageButton("image/totalUserButton.jpg", "Total User", "image/totalUserButtonOver.jpg");
		m_rightPanel.add(m_totalUserButton);
		m_totalUserButton.setBounds(20,544,65,44);
		m_totalUserButton.addActionListener(event);
		
		m_scoreButton = new ImageButton("image/backButton.jpg", "점수 보기", "image/backButtonOver.jpg");
		m_scoreButton.addActionListener(event);
		
		
		m_rightPanel.add(m_scoreButton);
		m_scoreButton.setBounds(130,544,65,44);
		
		m_rightPanel.add(m_gamerCaptin);
		m_gamerCaptin.setBounds(2,75,100,130);
		
		m_rightPanel.add(m_gamerChallenger);
		m_gamerChallenger.setBounds(112,75,100,130);
		
		m_rightPanel.add(m_gamerChallenger2);
		m_gamerChallenger2.setBounds(222,75,100,130);
		
		m_rightPanel.add(m_exitButton);
		m_exitButton.setBounds(240, 544, 65, 44);
		m_exitButton.addActionListener(event);
		
		m_rightPanel.setLayout(null);
		
		add(m_rightPanel);
		m_rightPanel.setBounds(615,5,320,600);
	}

	public void setTextToLogWindow(String str) {
		m_log.append(str);
	}

	public void setUserList(Vector<String> userList) {
Vector<String> temp = new Vector<String>();
		
		setRoomKing(userList.get(0));

		if(userList.size() < 1) {
			setChallenger("Please wait....");
			setChallenger2("Please wait....");
		} else if (userList.size() < 2){
			setChallenger(userList.get(1));
			setChallenger2("Please wait....");
		} else {
			setChallenger(userList.get(1));
			setChallenger(userList.get(1));
		}
		int i = 0;
		for (String user : userList)
			if (i++ > 1) temp.add(user);
	}

	private void setChallenger(String user) {
		m_gamerChallenger.setText(user);
		this.repaint();
	}
	
	private void setChallenger2(String user) {
		m_gamerChallenger2.setText(user);
		this.repaint();
	}

	private void setRoomKing(String user) {
		m_gamerCaptin.setText(user);
		this.repaint();
	}

	public void showMessageBox(String sender, String message, boolean option) {
		new SlipFrame(sender, message, false);
	}

	public JList getJList() {
		return m_userList;
	}

	public void unShow() {
		this.setVisible(false);
	}

	public String getInputText() {
		return m_input.getText();
	}


	public void setTextToInput(String string) {
		m_input.setText(string);
	}

	public void setRoomList(Vector<String> roomList) {
	}	
	
	
	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("Network Acid Rain");
		Container cp = frame.getContentPane();
		cp.add(new GameRoomGui(null));
		
		frame.setSize(970, 660);
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		System.out.println("Exit");
	}

	
	
	
	/* 
	 * Canvas __ GameBoard
	 */

	class GameBoardCanvas extends JComponent implements ActionListener{

		private static final long serialVersionUID = 1L;

		public GameBoardCanvas()
		{
			setLayout(null);
	         t1 = new JTextField(20);
	         t1.setBounds(100, 50, 400, 28);
	         t1.setLocation(130,550);
	        
	         t1.addActionListener(new TextFieldListener());
	         add(t1);
	         t1.setOpaque(true);
		}


		public void paint(Graphics g)
		{
			super.paint(g);
			
			g.setColor(Color.BLACK);
			g.setFont(new Font("나눔고딕", Font.BOLD, 12));
			g.drawString("  생  명\t" +remainingLife(life) +lostLife(max_life - life), 10, 20);
			g.drawString("  점  수\t" +score +"점", 10, 40);
			// 맞추거나 틀리거나 놓치는 등 각 상태에 맞게 얻거나 잃는 점수를 겹치지 않게 표시 
			if(wrong && (!miss && !dont_type && !get && !get_hidden && !get_gold && !get_diamond))
			{
				g.drawString("(-" +wrong_point +")", 130, 40);
			}
			if(miss && (!wrong && !dont_type && !get && !get_hidden && !get_gold && !get_diamond))
			{
				g.drawString("(-" +missing_point +")", 130, 40);
			}
			if(dont_type && (!wrong && !miss && !get && !get_hidden && !get_gold && !get_diamond))
			{
				g.drawString("(-" +dont_type_point +")", 130, 40);
			}
			if(get && (!wrong && !miss && !dont_type && !get_hidden && !get_gold && !get_diamond))
			{
				g.drawString("(+" +earning_point +")", 130, 40);
			}
			if(get_hidden && (!wrong && !miss && !dont_type && !get && !get_gold && !get_diamond))
			{
				g.drawString("(+" +point_for_hidden +")", 130, 40);
			}
			
			// 골드와 다이아 단어는 다른 문구로 출력 
			if(get_gold && (!wrong && !miss && !dont_type && !get && !get_hidden && !get_diamond))
			{
				g.setColor(new Color(231, 206, 180));
				g.drawString("골드 획득 ! (+" +point_for_gold +")", 130, 40);
				g.setColor(Color.BLACK);
			}
			if(get_diamond && (!wrong && !miss && !dont_type && !get && !get_hidden && !get_gold))
			{
				g.setColor(new Color(137,136,142));
				g.drawString("다이아몬드 획득 ! (+" +point_for_diamond +")", 130, 40);
				g.setColor(Color.BLACK);
			}
			
			g.drawString("남은시간\t" +(limit_time - (play_time / 10)) +"초", 10, 60);
			// 플레이 시간이 4분이 넘을 경우 점수 1.5배 알림 문구 출력 
			if(play_time > 2400)
			{
				g.drawString("(점수 1.5배 !)", 100, 60);
			}
			
			g.drawString("  레  벨\t" +level, 10, 80);
			if(blackout) // 정전 단어를 입력했을 시 알림 문구 출력 
			{
				g.drawString("정전 ! (" +(3 - (play_time - blackout_duration) / 10) +")", 120, 80);
			}
			
			// 아이템별로 아이템 획득 단어와 동일한 색상의 메뉴 출력 및 지속 시간이 있는 아이템의 경우 남은 지속 시간 출력 
			g.setColor(Color.BLACK);
			g.drawString(" [ 아이템 목록 ]", 10, 100);
			g.setColor(Color.RED);
			g.drawString("1. 생명 + 1\t" +item[0], 10, 120);
			g.setColor(Color.BLUE);
			g.drawString("2. 점수 x 2\t" +item[1], 10, 140);
			if(active_item_two)
			{
				g.drawString("(" +(15 - (play_time - item_two_duration) / 10) +")", 120, 140);
			}
			
			g.setColor(new Color(0, 154, 81));
			g.drawString("3. 무  적\t\t" +item[2], 10, 160);
			if(active_item_three)
			{
				g.drawString("(" +(10 - (play_time - item_three_duration) / 10) +")", 120, 160);
			}
			
			g.setColor(new Color(35, 63, 138));
			g.drawString("4. 점수+500\t" +item[3], 10, 180);
			g.setColor(new Color(155, 38, 152));
			g.drawString("5. +10/0.1s \t\t" +item[4], 10, 200);
			if(active_item_five)
			{
				g.drawString("(" +(10 - (play_time - item_five_duration) / 10) +")", 120, 200);
			}
			
			g.setColor(Color.GRAY);
			g.drawString("6. 화면 멈춤\t" +item[5], 10, 220);
			if(active_item_six)
			{
				g.drawString("(" +(5 - (play_time - item_six_duration) / 10) +")", 120, 220);
			}
			
			g.setColor(new Color(225, 177, 144));
			g.drawString("7. 슬로 모션\t" +item[6], 10, 240);
			if(active_item_seven)
			{
				g.drawString("(" +(10 - (play_time - item_seven_duration) / 10) +")", 120, 240);
			}
			
			g.setColor(new Color(255, 197, 0));
			g.drawString("8. 영어 병행\t" +item[7], 10, 260);
			if(active_item_eight)
			{
				g.drawString("(" +(40 - (play_time - item_eight_duration) / 10) +")", 120, 260);
			}
			
			g.setColor(new Color(0, 255, 192));
			g.drawString("9. 초기화\t\t" +item[8], 10, 280);
			
			g.setColor(Color.BLACK);
			g.drawString("0. 돋보기\t\t" +item[9], 10, 300);
			if(active_item_ten)
			{
				g.drawString("(" +(10 - (play_time - item_ten_duration) / 10) +")", 120, 300);
			}
			
			// 시간이 지남에 따라 증가하는 산성도를 표시 
			g.setColor(Color.BLACK);
			g.drawString(" [ 산성도 ] ", 10, 320);
			
			// R, G, B 초기값으로 설정
			g.setColor(new Color(red , green, blue));
			if(play_time < 500) // 0초 - 50초 동안 
			{
				// pH 6에 가까운 색으로 서서히 조절 
				if(play_time % 10 == 0)
				{
					red++;
				}
				if(play_time % 20 == 0)
				{
					green++;
				}
			}
			else if(play_time >= 500 && play_time < 1000) // 50초 - 100초 동안 
			{
				// pH 5에 가까운 색으로 서서히 조절 
				if(play_time % 20 == 0)
				{
					red++;
				}
			}
			else if(play_time >= 1000 && play_time < 1500) // 100초 - 150초 동안 
			{
				// pH 4에 가까운 색으로 서서히 조절 
				if(play_time % 100 == 0)
				{
					red += 3;
				}
				if(play_time % 50 == 0)
				{
					green--;
				}
				if(play_time % 50 == 0)
				{
					blue++;
				}
			}
			else if(play_time >= 1500 && play_time < 2000) // 150초 - 200초 동안 
			{
				// pH 3에 가까운 색으로 서서히 조절 
				if(play_time % 50 == 0 && red < 255)
				{
					red++;
				}
				if(play_time % 100 == 0)
				{
					green -= 9;
				}
			}
			else if(play_time >= 2000 && play_time < 2500) // 200초 - 250초 동안 
			{
				// pH 2에 가까운 색으로 서서히 조절 
				if(play_time % 100 == 0)
				{
					green -= 7;
				}
				if(play_time % 50 == 0)
				{
					blue--;
				}
			}
			else // 250초 - 300초 동안 
			{
				// pH 1에 가까운 색으로 서서히 조절 
				if(play_time % 100 == 0)
				{
					green--;
				}
			}
				
			// 특정 색으로 직사각형을 출력 
			g.fillRect(13, 330, 50, 200);
			g.setColor(Color.BLACK);
			
			// pH 농도 라인 표시 
			g.drawLine(13, ph_line, 80, ph_line);
			g.drawString("pH: " +ph, 85, ph_line + 3);
			
			// 아이템 획득 단어의 경우 아이템의 색상과 같이 출력되도록 설정 
			for(Word w: viewingWords)
			{
				if(blackout) // 정전 단어 입력 시 글자색을 투명한 색으로 설정  
				{
					g.setColor(new Color(0, 0, 0, 0));
				}
				else if(w.str.equals(hidden[0]))
				{
					g.setColor(Color.RED);
				}
				else if(w.str.equals(hidden[1]))
				{
					g.setColor(Color.BLUE);
				}
				else if(w.str.equals(hidden[2]))
				{
					g.setColor(new Color(0, 154, 81));
				}
				else if(w.str.equals(hidden[3]))
				{
					g.setColor(new Color(35, 63, 138));
				}
				else if(w.str.equals(hidden[4]))
				{
					g.setColor(new Color(155, 38, 152));
				}
				else if(w.str.equals(hidden[5]))
				{
					g.setColor(Color.GRAY);
				}
				else if(w.str.equals(hidden[6]))
				{
					g.setColor(new Color(225, 177, 144));
				}
				else if(w.str.equals(hidden[7]))
				{
					g.setColor(new Color(255, 197, 0));
				}
				else if(w.str.equals(hidden[8]))
				{
					g.setColor(new Color(0, 255, 192));
				}
				else if(w.str.equals(donttype[0]) || w.str.equals(donttype[1]) || w.str.equals(donttype[2]))
				{
					g.setColor(Color.MAGENTA);
				}
				else if(w.str.equals(gold))
				{
					g.setColor(new Color(231, 206, 180));
				}
				else if(w.str.equals(diamond))
				{
					g.setColor(new Color(137, 136, 142));
				}
				else
				{
					g.setColor(Color.BLACK);
				}
				
				// 아이템 0번 사용 시 일시적으로 글자 크기를 확대 
				if(active_item_ten)
				{
					g.setFont(new Font("나눔고딕", Font.BOLD, 15));
				}
				
				// 각 단어의 내용을 x, y좌표에 출력 
				g.drawString(w.str, w.x, w.y);
			}
			
			// 아이템 8번 사용 시 영어 단어 출력 
			if(active_item_eight)
			{
				for(Word w_e: viewingWords_eng)
				{
					g.setColor(Color.BLACK);
					
					// 영어 단어도 아이템 0번 사용 시 일시적으로 글자 크기를 확대  
					if(active_item_ten)
					{
						g.setFont(new Font("나눔고딕", Font.BOLD, 15));
					}

					if(blackout)
					{
						g.setColor(new Color(0, 0, 0, 0));
					}
					
					g.drawString(w_e.str_e, w_e.x, w_e.y);
				}
			}
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class Word
	{
		static final int WIDTH = 590;
		public int x; // 단어의 x좌표 
		public int y; // 단어의 y좌표 
		String str; // 한글 단어의 내용 
		String str_e; // 영어 단어의 내용 
		
		Word()
		{
			x = (int)(Math.random() * (WIDTH - 300) + 200); // x좌표는 제한된 공간에 임의의 좌표 
			y = 0; // y좌표는 화면 최상단 
			str = words.get((int)(Math.random() * words.size())); // 임의의 단어를 읽어들여 저장 
			
			if(active_item_eight) // 아이템 8번 사용 시 영어 단어는 다른 변수에 영어 단어 저장 
			{
				str_e = words_eng.get((int)(Math.random() * words_eng.size()));
			}
		} // Word()
	} // Word Class

	
	public void setTotalUser(Vector<String> userList) {
		userListFrame.setUserList(userList);
	}

	public void setUserListFrame(UserListFrame userListFrame) {
		this.userListFrame = userListFrame;
	}

	public void setGameRoomInfo(String info) {
		StringTokenizer token = new StringTokenizer(info, "|");
		
		m_titleLabel.setText(token.nextToken());
		m_gamer1.setText(token.nextToken());
//		m_gamer2.setText(token.nextToken());
	}

	public int[] getFrameSize() {
		int[] size = {970, 660};
		return size;
	}
	public void setPanel(PanelInterface panel) {
	}
	public void setPanel(GameLobbyInter panel) {
	}
	public void setPanel(LobbyGuiInter panel) {
	}
	public void setPanel(RoomGuiInter panel) {
	}

	public GameBoardCanvas getM_board() {
		return m_board;
	}
}