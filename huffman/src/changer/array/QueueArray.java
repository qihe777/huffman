package changer.array;

public class QueueArray {
    private byte[] array;
    private int head;
    private int rear;
    private int size;
    private int cap;
    public QueueArray(){
        cap=1024;
        array=new byte[cap];
        head=rear=size=0;
    }
    public int flush(){
        if(size%8!=0){
            int x=8-(size%8);
            byte[] bytes=new byte[x];
            enqueue(bytes);
            return x;
        }
        else
            return 0;

    }
    public boolean enqueue(byte[] code){
        if(code.length+size>cap)
            return false;
        for(byte tmpByte:code){
            size++;
            array[rear]=tmpByte;
            rear = (rear+1)%array.length;
        }
        //System.out.println("head"+head+"size"+size+"rear"+rear+Arrays.toString(array));
        return true;

    }
    //出队出的是byte
    public Byte dequeue(){
        //System.out.println("head"+head+"size"+size+"rear"+rear+Arrays.toString(array));
        if(size<8)
            return null;
        size=size-8;
        //直接使用将二进制转化为十进制的方法
        int x=0;
        for(int i=0;i<8;i++){
            x=x*2+array[head];
            head = (head+1)%array.length;
        }
        return (byte)x;
    }
    //获取当前存储的数据相当多少byte
    public boolean isEmpty(){
        return size==0;
    }
}
