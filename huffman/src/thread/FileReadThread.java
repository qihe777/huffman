package thread;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileReadThread extends Thread{
    private String path;
    private int defaultLength=1024;
    //不太清楚设置为多大
    final BlockingQueue<byte[]> blockingQueue=new ArrayBlockingQueue<>(20);
    public FileReadThread(String path){
        this.path=path;
    }

    public byte[] nextBatch() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public void run() {
        //super.run();
        FileInputStream fis= null;
        LoopArray loopArray=new LoopArray();
        //创建一个长度为1024的读取缓存
        byte[] bbuf=loopArray.get();

        try {
            fis = new FileInputStream(path);
            //用于保存实际读取的字节数
            int hasRead=0;
            byte[] lastBytes=null;
            while((hasRead=fis.read(bbuf))>0){
                //放到阻塞队列中
                //如果是文件末尾
                if(hasRead<defaultLength){
                    //新建一个数组
                    lastBytes=new byte[hasRead];
                    System.arraycopy(bbuf,0,lastBytes,0,hasRead);
                    bbuf=lastBytes;
                }
                blockingQueue.put(bbuf);
                bbuf=loopArray.get();
            }
            //如果文件末尾正好是1024，则放入一个空的
            if(lastBytes==null){
                System.out.println("文件读取完成，正好");
                blockingQueue.put(new byte[0]);
            }
            fis.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
