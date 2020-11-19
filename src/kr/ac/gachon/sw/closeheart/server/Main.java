package kr.ac.gachon.sw.closeheart.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.db.DBManager;

public class Main {

	private static Set<String> names = new HashSet<>();
	private static Set<PrintWriter> writers = new HashSet<>();

	private static int memberCnt = 1;	// �닚�꽌 ��濡� �뱾�뼱�삩 �겢�씪�씠�뼵�듃�겮由� ���솕 �븯�룄濡�, �겢�씪�씠�뼵�듃�쓽 �닽�옄瑜� 移댁슫�듃
	
	private static HashMap<Integer, PrintWriter> map = new HashMap<>();	// �겢�씪�씠�뼵�듃 + outStream
	private static HashMap<Integer, Boolean> isOnline = new HashMap<>();	// �겢�씪�씠�뼵�듃 + state

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
		private boolean state; // �쁽�옱 �젒�냽 �빐 �엳�뒗 吏�, �뾾�뒗 吏�

		private int in_number; // 紐� 踰덉㎏濡� �뱾�뼱�삩 client�씤吏�

		public Handler(Socket socket) {
			this.socket = socket;
			in_number = memberCnt;	// �빐�떦 �겢�씪�씠�뼵�듃 number
			memberCnt++;
		}

		public void run() {
			try {
				
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				// name =
				// db�뿉�꽌 �씫�뼱�샂
				
				this.state = true;
				map.put(in_number, out);
				isOnline.put(in_number, state);

				while(memberCnt % 2 != 0);	// 理쒖냼 1紐� �씠�긽�쓽 ���솕 �긽��媛� �엳�뼱�빞 while �깉異�
				
				if(in_number % 2 == 0) {	// 吏앹닔 (���솕 �긽��媛� 癒쇱� �뱾�뼱�삩 寃쎌슦)
					if(isOnline.get(in_number - 1)) {	// �긽��諛⑹씠 �삩�씪�씤�씠硫�,
						out = map.get(in_number - 1);
					}
					
				} else {	// ���닔 (���솕 �긽��媛� 諛붾줈 �떎�쓬�뿉 �뱾�뼱�삩 寃쎌슦)
					if (in_number + 1 <= memberCnt && isOnline.get(in_number + 1)) {
						// �긽��諛⑹씠 �삩�씪�씤�씠硫�
						out = map.get(in_number + 1);
					}
				}
				
				// Accept messages from this client and broadcast them.
				while (true) {
					
					String input = in.nextLine(); // �겢�씪�씠�뼵�듃濡� 遺��꽣,
					if (input.toLowerCase().startsWith("/quit")) {	// 梨꾪똿 �걹
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
