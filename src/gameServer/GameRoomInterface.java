package gameServer;

import protocolData.Protocol;

public interface GameRoomInterface extends ServerInterface {

	String getRoomName();
	String getRoomKingName();
    int getNumber();
	boolean isAllReady();
	void sendTo(int toGamer, Protocol data);
	void exitRoomMaster(Protocol data);
	boolean isStart();
	void setStart(boolean isStart);
	int getUserCounter();
}
