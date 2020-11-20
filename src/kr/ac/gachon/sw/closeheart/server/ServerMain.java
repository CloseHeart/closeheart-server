package kr.ac.gachon.sw.closeheart.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
	public static void main(String[] args) {
		// Login Server 가동 
		// 서버 시작시에는 무조건 Login Server만 돌고 있음
		ExecutorService loginServerPool = Executors.newFixedThreadPool(1000);
		
		try (ServerSocket listener = new ServerSocket(21325)) {
			// 서버 시작 알림 Print
			System.out.println("User Server Starting....");
			while (true) {
				loginServerPool.execute(new LoginServer.loginServerHandler(listener.accept()));
			}
		} catch (Exception e) {
			System.out.println("User Server Start Failed! - " + e.getMessage());
		}	
	}
}
