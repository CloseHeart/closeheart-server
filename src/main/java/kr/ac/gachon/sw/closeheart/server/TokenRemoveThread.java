package kr.ac.gachon.sw.closeheart.server;

import kr.ac.gachon.sw.closeheart.server.db.DBConnect;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TokenRemoveThread extends Thread {
    @Override
    public void run() {
        // 10분마다 Remove시킴
        System.out.println("[Token Remover] Token Remover Enabled");
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleAtFixedRate(new Remover(), 0, 10, TimeUnit.MINUTES);
    }

    class Remover implements Runnable {
        @Override
        public void run() {
            try {
                // 만료 토큰 삭제
                boolean isRemoved = DBConnect.removeExpiredToken();

                // 메시지 및 성공 여부 출력
                System.out.println("[Token Remover] Expired Token Removed! - return " + isRemoved);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
