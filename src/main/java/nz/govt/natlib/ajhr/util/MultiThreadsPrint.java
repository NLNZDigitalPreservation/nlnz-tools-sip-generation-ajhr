package nz.govt.natlib.ajhr.util;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiThreadsPrint {
    private static final ScheduledThreadPoolExecutor _schedule_executor = new ScheduledThreadPoolExecutor(256);
    private static final String lineSeparator = System.lineSeparator();
    private static final Map<String, Message> msgQueue = new HashMap<>();
    private static int countPreviousUnfinished = 0;


    public static void init() {
        Runnable handler = new Runnable() {
            @Override
            public void run() {
                print();
            }
        };

        _schedule_executor.scheduleWithFixedDelay(handler, 500, 500, TimeUnit.MILLISECONDS);
    }

    synchronized private static void print() {
        System.out.print("\r".repeat(countPreviousUnfinished));
        StringBuilder unfinishedBuf = new StringBuilder();
        StringBuilder finishedBuf = new StringBuilder();
        List<String> finishedKey = new ArrayList<>();
        msgQueue.values().forEach(msg -> {
            if (msg.isFinished()) {
                finishedKey.add(msg.getKey());
                finishedBuf.append(msg.getInfo()).append(lineSeparator);
            } else {
                unfinishedBuf.append(msg.getInfo()).append(lineSeparator);
            }
        });
        countPreviousUnfinished = msgQueue.size() - finishedKey.size();
        finishedKey.forEach(msgQueue::remove);
        System.out.print(finishedBuf.toString());
        System.out.print(unfinishedBuf.toString());
    }

    synchronized private static void put(String key, String info, boolean finished) {
        Message msg;
        if (msgQueue.containsKey(key)) {
            msg = msgQueue.get(key);
        } else {
            msg = new Message();
        }
        msg.setKey(key);
        msg.setFinished(finished);
        msg.setInfo(info);
        msgQueue.put(key, msg);
    }


    public static void putFinished(String info) {
        String key = UUID.randomUUID().toString();
        put(key, info, true);
    }

    public static String putUnFinished(String info) {
        String key = UUID.randomUUID().toString();
        put(key, info, false);
        return key;
    }

    public static void putUnFinished(String key, String info) {
        put(key, info, false);
    }

    static class Message {
        private boolean finished;
        private String info;
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}