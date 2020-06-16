package gui;

import gui.GameRoomGui.GameBoardCanvas;

import java.util.Vector;

public interface RoomGuiInter extends PanelInterface {
	int isRoomKingExit=0;
	void setGameRoomInfo(String info);
	void setRoomList(Vector<String> roomList);
	GameBoardCanvas getM_board();
}
