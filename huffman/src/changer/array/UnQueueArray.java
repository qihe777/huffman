package changer.array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnQueueArray {
    private byte[] array;
    private int head;
    private int rear;
    //当前存储数量
    private int size;
    //最大值
    private int cap;
    private int move;
    private Map<Integer, List<MyNode>> lengthMap;
    class MyNode{
        byte key;
        byte[] code;
        MyNode( byte key,byte[] code){
            this.key=key;
            this.code=code;
        }
    }
    public UnQueueArray(Map<Byte,byte[]> codeMap){
        cap=1024;
        array=new byte[cap];
        head=rear=size=move=0;
        //将codemap预处理一下，加快速度
        lengthMap=new HashMap<>();
        for(Map.Entry<Byte,byte[]> entry:codeMap.entrySet()){
            List<MyNode> tmp= lengthMap.getOrDefault(entry.getValue().length,new ArrayList<>());
            tmp .add(new MyNode(entry.getKey(),entry.getValue()));
            lengthMap.put(entry.getValue().length,tmp);
        }
        //lengthMap.forEach((key, value) -> System.out.println(key + "->" + value.size()));

    }
    //将尾部的0删去
    public void flush(int zeroNum){
        rear=(rear-zeroNum+array.length)%array.length;
        size-=zeroNum;
    }
    public boolean enqueue(byte b){
        if(8+size>cap)
            return false;
        for(int m=7;m>=0;m--){
            array[rear]=(byte)(b>>>m & 1);
            rear = (rear+1)%array.length;
        }
        size+=8;
        return true;
    }
    //出队出的是byte
    public Byte dequeue(){
        //System.out.println(Arrays.toString(array));
        //如果当前没有匹配的编码，则返回null
        int moveLength=(move-head+array.length)%array.length+1;
        while (moveLength<=size){
            //System.out.println("匹配"+moveLength);
            //判断当前head到move能否匹配到编码
            if(lengthMap.containsKey(moveLength)){
                //System.out.println(Arrays.toString(array));
                for(MyNode myNode:lengthMap.get(moveLength)){
                    //System.out.println("有长度为"+moveLength);
                    //开始逐一字段匹配两个数组
                    for(int i=0;i<moveLength;i++){
                        if(myNode.code[i]!=array[(head+i)%array.length]){
                            break;
                        }
                        //如果找到了
                        if(i==moveLength-1){
                            //匹配到了则直接返回byte
                            size-=moveLength;
                            move=(move+1)%array.length;
                            head=move;
                            return myNode.key;
                        }
                    }
                }
            }
            move++;
            moveLength++;
        }
        return null;
    }
    //获取当前存储的数据相当多少byte
    public boolean isEmpty(){
        return size==0;
    }
}
