package changer;

import changer.array.QueueArray;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CompressByteContainer {
    //缓存，每次刷新时全刷新
    private byte[] cache;
    private int cachePos;
    private int almostPos;
    private int cacheSize;
    private String path;
    //01数组转化为byte字节。循环队列
    private QueueArray queueArray;
    private FileOutputStream fileOutputStream;
    public CompressByteContainer(String path){
        cachePos=0;
        cacheSize=10*1024;
        almostPos=9*1024;
        cache=new byte[cacheSize];
        queueArray=new QueueArray();
        this.path=path;
        try {
            //从末尾开始写
            fileOutputStream=new FileOutputStream(path,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] number){
        //如果加入队列失败则刷新后重新加入。
        if(!queueArray.enqueue(number)){
            //System.out.println("转化数组已经满");
            arrayFlush();
            if(!queueArray.enqueue(number)){
                System.out.println("压缩队列刷新失败，仍然无法添加数据");
            }
        }
    }
    //关闭的时候需要将全部的内容刷新
    public void close(){
        int x=queueArray.flush();
        arrayFlush();
        //检查是否还有剩余
        if(!queueArray.isEmpty()){
            System.out.println("压缩出现错误，循环队列仍有参与数据");
        }
        cacheFlush();
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            RandomAccessFile randomFile = new RandomAccessFile(path, "rw");
            //只会写入一个字节
            randomFile.write(x);
            System.out.println("补零长度为"+x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //刷新队列到缓存，当队列不够用时触发操作。
    private void arrayFlush(){
        //首先判断缓存是否够用，这是粗略判断，因为不想在循环队列中增加当前有多少数据的字段了。
        if(cachePos>almostPos){
            cacheFlush();
        }
        //写到缓存中,即将每8位01串转化为byte类型数据。
        Byte tmpByte;
        while((tmpByte=queueArray.dequeue())!=null){
            cache[cachePos++]=tmpByte;
            //System.out.println("写入byte:"+tmpByte);
        }
    }
    //刷新数据数据写入到文件中。当缓存不够用时触发操作
    private void cacheFlush(){
        try {
            fileOutputStream.write(cache,0,cachePos);
            cachePos=0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
