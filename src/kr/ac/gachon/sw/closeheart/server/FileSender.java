package kr.ac.gachon.sw.closeheart.server;

import java.io.*;
import java.net.Socket;

/**
 * 파일 전송 클래스
 *
 */
public class FileSender extends Thread{
    private Socket socket;
    private String filename;
    private DataOutputStream dos;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private int control = 0;

    public FileSender(Socket socket,String filestr) {
        this.socket = socket;
        this.filename = filestr;
        try {
            // 데이터 전송용 스트림 생성
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String fName = filename;

            // 파일 내용을 읽으면서 전송
            File f = new File(fName);
            fis = new FileInputStream(f);
            bis = new BufferedInputStream(fis);

            int len;    // 파일에서 읽어온 데이터 길이
            int size = 4096;
            byte[] data = new byte[size];

            while ((len = bis.read(data)) != -1) {
                control++;
                if(control % 10000 == 0)
                {
                    System.out.println("Sending..." + control/10000);
                }
                dos.write(data, 0, len);
            }

            dos.flush();
            dos.close();
            bis.close();
            fis.close();
            System.out.println("File sending is completed.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
