package thread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileWriterThread extends Thread{
    private String path;
    private int defaultLength=1024;
    //不太清楚设置为多大
    final BlockingQueue<byte[]> blockingQueue=new ArrayBlockingQueue<byte[]>(20);
    public FileWriterThread(String path){
        this.path=path;
    }

    public void addBatch(byte[] bytes) throws InterruptedException {
        blockingQueue.put(bytes);
    }

    @Override
    public void run() {
        super.run();
        FileOutputStream fos= null;
        byte[] bbuf;
        try {
            fos = new FileOutputStream(path,true);
            //用于保存实际读取的字节数
            while(true){
                bbuf=blockingQueue.take();
                fos.write(bbuf);
                //如果是文件末尾
                if(bbuf.length<defaultLength){
                    break;
                }
            }
            fos.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
