package changer;

import changer.array.UnQueueArray;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class UnCompressByteContainer {
    //缓存，每次刷新时全刷新
    private byte[] cache;
    private int cachePos;
    private int almostPos;
    private int cacheSize;
    private String path;
    private int zeroNum;
    //循环队列存放01字符串
    private UnQueueArray queueArray;
    private FileOutputStream fileOutputStream;
    private Map<Byte,byte[]> codeMap;
    public UnCompressByteContainer(String path, Map<Byte,byte[]> codeMap,int zeroNum){
        cachePos=0;
        cacheSize=10*1024;
        almostPos=9*1024;
        cache=new byte[cacheSize];
        queueArray=new UnQueueArray(codeMap);
        this.path=path;
        this.codeMap=codeMap;
        this.zeroNum=zeroNum;
        try {
            //从末尾开始写
            fileOutputStream=new FileOutputStream(path,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //将字节转化成01数组放到转化器中
    public void write(byte b){
        //如果加入队列失败则刷新后重新加入。
        if(!queueArray.enqueue(b)){
            arrayFlush();
            if(!queueArray.enqueue(b)){
                System.err.println("解压队列刷新失败，仍然无法添加数据");
            }
        }
    }

    //关闭的时候，将缓存中最后的0删去，然后进行刷新。
    public void close(){
        queueArray.flush(zeroNum);
        arrayFlush();
        //检查是否还有剩余
        if(!queueArray.isEmpty()){
            System.err.println("出现错误，循环队列仍有残余数据");
        }
        cacheFlush();
        try {
            fileOutputStream.close();
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
        //写到缓存中,将01字符串匹配编码。
        Byte tmpByte;
        while((tmpByte=queueArray.dequeue())!=null){
            cache[cachePos++]=tmpByte;
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
