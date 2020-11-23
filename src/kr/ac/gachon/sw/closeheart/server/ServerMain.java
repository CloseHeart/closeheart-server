package kr.ac.gachon.sw.closeheart.server;

public class ServerMain {
	static int loginServerPort = 21325;
	static int friendServerPort = 21326;

	public static void main(String[] args) {
		// 로그인 서버 Thread
		LoginServer loginServer = new LoginServer(loginServerPort);

		// 친구 서버 Thread
		FriendServer friendServer = new FriendServer(friendServerPort);

		loginServer.start();
		friendServer.start();
	}
}
