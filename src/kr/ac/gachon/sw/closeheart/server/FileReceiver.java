package kr.ac.gachon.sw.closeheart.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 파일 받는 클래스
 */
public class FileReceiver extends Thread{
    private Socket socket;
    private DataInputStream dis;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private String filename;
    private long start;
    private int control = 0;

    public FileReceiver(Socket socket,String filestr,long starttime) {
        this.socket = socket;
        this.filename = filestr;
        this.start = starttime;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            String fName = filename;

            // 파일을 생성하고 파일에 대한 출력 스트림 생성
            File f = new File(fName);
            fos = new FileOutputStream(f);
            bos = new BufferedOutputStream(fos);

            // 바이트 데이터를 전송받으면서 기록
            int len;
            int size = 4096;
            byte[] data = new byte[size];

            while ((len = dis.read(data)) != -1) {
                control++;
                if (control % 10000 == 0) {
                    System.out.println("Receiving..." + control / 10000);
                }
                bos.write(data, 0, len);
            }

            long end = System.currentTimeMillis();
            System.out.println("Elapsed Time (seconds) : " + (end - start) / 1000.0);
            bos.flush();
            bos.close();
            fos.close();
            dis.close();

            System.out.println("File receiving is completed.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
