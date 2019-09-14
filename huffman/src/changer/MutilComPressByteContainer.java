package changer;

import changer.array.QueueArray;
import thread.LoopArray;
import thread.FileWriterThread;

public class MutilComPressByteContainer {
    //缓存，每次刷新时全刷新
    private byte[] cache;
    private int cachePos;
    private int cacheSize;
    //01数组转化为byte字节。循环队列
    public QueueArray queueArray;
    private FileWriterThread fileWriter;
    private LoopArray loopArray;
    public MutilComPressByteContainer(FileWriterThread fileWriter){
        cachePos=0;
        cacheSize=1024;
        //cache=new byte[cacheSize];
        loopArray=new LoopArray();
        cache=loopArray.get();
        queueArray=new QueueArray();
        this.fileWriter=fileWriter;
    }

    public void write(byte[] number){
        //如果加入队列失败则刷新后重新加入。
        if(!queueArray.enqueue(number)){
            arrayFlush();
            if(!queueArray.enqueue(number)){
                System.err.println("压缩队列刷新失败，仍然无法添加数据");
            }
        }
    }
    //关闭的时候需要将全部的内容刷新
    public int close(){
        int x=queueArray.flush();
        arrayFlush();
        //检查是否还有剩余
        if(!queueArray.isEmpty()){
            System.out.println("压缩出现错误，循环队列仍有参与数据");
        }
        byte[] bytes=new byte[cachePos];
        System.arraycopy(cache,0,bytes,0,cachePos);
        try {
            fileWriter.addBatch(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return x;
    }
    //刷新队列到缓存，当队列不够用时触发操作。
    private void arrayFlush(){
        Byte tmpByte;
        while((tmpByte=queueArray.dequeue())!=null){
            if(cachePos==cacheSize){
                try {
                    fileWriter.addBatch(cache);
                    cachePos=0;
                    cache=loopArray.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cache[cachePos++]=tmpByte;
        }
    }
}
