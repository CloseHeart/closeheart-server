  
package kr.ac.gachon.sw.closeheart.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

	private static Set<String> names = new HashSet<>();
	private static Set<PrintWriter> writers = new HashSet<>();

	private static int memberCnt = 1;	// 순서 대로 들어온 클라이언트끼리 대화 하도록, 클라이언트의 숫자를 카운트

	private static HashMap<Integer, PrintWriter> map = new HashMap<>();	// 클라이언트 + outStream
	private static HashMap<Integer, Boolean> isOnline = new HashMap<>();	// 클라이언트 + state

	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running...");
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(59001)) {
			while (true) {
				pool.execute(new Handler(listener.accept()));
			}
		}
	}

	private static class Handler implements Runnable {
		private String name;
		private Socket socket;
		private Scanner in;
		private PrintWriter out;
		private boolean state; // 현재 접속 해 있는 지, 없는 지

		private int in_number; // 몇 번째로 들어온 client인지

		public Handler(Socket socket) {
			this.socket = socket;
			in_number = memberCnt;	// 해당 클라이언트 number
			memberCnt++;
		}

		public void run() {
			try {
				
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				// name =
				// db에서 읽어옴
				
				this.state = true;
				map.put(in_number, out);
				isOnline.put(in_number, state);

				while(memberCnt % 2 != 0);	// 최소 1명 이상의 대화 상대가 있어야 while 탈출
				
				if(in_number % 2 == 0) {	// 짝수 (대화 상대가 먼저 들어온 경우)
					if(isOnline.get(in_number - 1)) {	// 상대방이 온라인이면,
						out = map.get(in_number - 1);
					}
					
				} else {	// 홀수 (대화 상대가 바로 다음에 들어온 경우)
					if (in_number + 1 <= memberCnt && isOnline.get(in_number + 1)) {
						// 상대방이 온라인이면
						out = map.get(in_number + 1);
					}
				}
				
				// Accept messages from this client and broadcast them.
				while (true) {
					
					String input = in.nextLine(); // 클라이언트로 부터,
					if (input.toLowerCase().startsWith("/quit")) {	// 채팅 끝
						return;
					}
					
					out.println(input);
				}
				
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				if (out != null) {
					writers.remove(out);
				}
				if (name != null) {
					state = false;
					isOnline.remove(in_number);
					isOnline.put(in_number, state);
					names.remove(name);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}