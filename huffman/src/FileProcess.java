import changer.CompressByteContainer;
import changer.MutilComPressByteContainer;
import changer.UnCompressByteContainer;
import helpbean.Heap;
import helpbean.Node;
import thread.FileReadThread;
import thread.FileWriterThread;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileProcess {
    private static byte[] code=new byte[256];
    //递归的方式来求编码
    static void dfs(Node head, int pos, Map<Byte,byte[]> codeMap){
        //如果是叶子节点,则将编码保存到map中
        if(head.right==null&&head.left==null){
            byte[] mycode=new byte[pos];
            System.arraycopy(code,0,mycode,0,pos);
            codeMap.put(head.myByte,mycode);
            return;
        }
        if(head.left!=null){
            code[pos]=0;
            dfs(head.left,pos+1,codeMap);
        }
        if(head.right!=null){
            code[pos]=1;
            dfs(head.right,pos+1,codeMap);
        }
    }

    //先生成huffman树，然后生成huffman编码
    public static Map<Byte,byte[]> getHuffmanCode(Map<Byte,Long> frequencyMap){
        //先生成huffman节点,放入到堆构造的优先队列中
        Heap heap=new Heap();
        for(Map.Entry<Byte,Long> entry:frequencyMap.entrySet()){
            heap.add(new Node(entry.getKey(),entry.getValue()));
        }
        //生成huffman树：每次取出来两个最小的，然后放进去node
        while (heap.getNum()>1){
            Node tmp1=heap.poll(),tmp2=heap.poll();
            heap.add(new Node(tmp1.frequency+tmp2.frequency,tmp1,tmp2));
        }
        //生成huffman编码：遍历所有的叶子节点即可,且设左节点为0右节点为1
        Map<Byte,byte[]> codeMap=new HashMap<>();
        dfs(heap.poll(),0,codeMap);
        heap.clear();
        return codeMap;
    }

    public static void writeHead(String outPath,Map<Byte,byte[]> codeMap){
        System.out.print("3.写入文件头信息：");
        try {
            FileOutputStream fileOutputStream=new FileOutputStream(outPath);
            fileOutputStream.write((byte)0);
            //种类总数两个字节先写低位，再写高位
            //codeMap.forEach((k,v)->System.out.println(k+"->"+Arrays.toString(v)));
            System.out.println("编码的个数"+codeMap.size());
            fileOutputStream.write(new byte[]{(byte)(codeMap.size() & 0xff),(byte)((codeMap.size() >> 8) & 0xFF)});
            for(Map.Entry<Byte,byte[]> entry:codeMap.entrySet()){
                fileOutputStream.write(entry.getKey());
                byte[] tmp=entry.getValue();
                int increase,tmpLength;
                if((increase=tmp.length%8)==0){
                    tmpLength=tmp.length/8;
                }else{
                    tmpLength=tmp.length/8+1;
                    increase=8-increase;
                }
                //编码的位数
                fileOutputStream.write((byte)tmpLength);
                //加入0的个数
                fileOutputStream.write((byte)increase);
                byte[] result=new byte[256];
                int tmPos=0,resultPos=0;
                //将01编码转化成byte写入文件
                for(int i=0;i<tmpLength;i++){
                    //每8位转化成一个byte
                    int x=0;
                    //数组索引小的是二进制中的高位
                    for(int j=0;j<8;j++){
                        if(tmPos<tmp.length){
                            x=x*2+tmp[tmPos++];
                        }
                        else
                            x=x<<1;
                    }
                    result[resultPos++]=(byte)x;
                }
                fileOutputStream.write(result,0,tmpLength);
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //压缩操作
    public static void comPress(String path){
        long startTime=System.currentTimeMillis();
        //省去检验路径和文件是否合法的操作。
        System.out.println("----开始进行压缩操作-----");
        //1.第一遍读取文件统计byte频率，保存到map中
        Map<Byte,Long> frequencyMap=new HashMap<>();
        System.out.println("1.统计字符频率");
        FileInputStream fis= null;
        //创建一个长度为10*1024的缓存
        byte[] bbuf=new byte[10*1024];
        try {
            fis = new FileInputStream(path);
            //用于保存实际读取的字节数
            int hasRead=0;
            while((hasRead=fis.read(bbuf))>0){
                for(int i=0;i<hasRead;i++) {
                    frequencyMap.put(bbuf[i], frequencyMap.getOrDefault(bbuf[i], (long) 0) + 1);
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //2.根据频率生成huffman编码
        System.out.println("2.生成huffman编码");
        Map<Byte,byte[]> codeMap=getHuffmanCode(frequencyMap);
        System.out.println("第一次读文件用时(ms)"+(System.currentTimeMillis()-startTime));
        //3.将数据格式写入到文件头。
        String outPath="out.huf";
        writeHead(outPath,codeMap);

        //4.第二遍读取文件,找到字节对应的编码，写入到位流读写器中。
        System.out.println("4.再次读取源文件，开始进行压缩");
        CompressByteContainer byteContainer=new CompressByteContainer(outPath);
        long fileLength=0;
        try {
            File file=new File(path);
            fileLength=file.length();
            long posLength=fileLength/10,allLength=0;
            fis = new FileInputStream(file);
            //用于保存实际读取的字节数
            int hasRead=0;
            long nowLength=0;
            while((hasRead=fis.read(bbuf))>0){
                for(int i=0;i<hasRead;i++) {
                    byteContainer.write(codeMap.get(bbuf[i]));
                }
                nowLength+=hasRead;
                if(nowLength>posLength){
                    allLength+=nowLength;
                    nowLength=0;
                    System.out.println("进度："+(float)allLength/fileLength);
                }
            }
            byteContainer.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime=System.currentTimeMillis();
        System.out.println(String.format("共用时%d ms", (endTime-startTime)));
        System.out.println("压缩率为："+  (float)new File("out.huf").length()/fileLength);
        System.out.println("-----压缩完成，得到文件：out.huf-------");
    }
    //普通的压缩操作
    public static void unCompress(String path,String name){
        long startTime=System.currentTimeMillis();
        //省去检验路径和文件是否合法的操作。
        System.out.println("----开始进行解压缩的操作----");
        System.out.println("1.正在读取文件头部:");
        //读取补零的长度
        byte[] littleBytes=new byte[3];
        byte[] codeBytes=new byte[256];
        Map<Byte,byte[]> codeMap=new HashMap<>();
        FileInputStream fis;
        long fileLength;
        try {
            File file=new File(path);
            fileLength=file.length();
            fis = new FileInputStream(file);
            fis.read(littleBytes);
            int last0=littleBytes[0];
            int typeNum=((littleBytes[2]&0xff)<<8)|littleBytes[1]&0xff;
            System.out.println("补零个数"+last0+",种类总数："+typeNum);
            for(int i=0;i<typeNum;i++){
                fis.read(littleBytes);
                //System.out.println(Arrays.toString(littleBytes));
                //将读取到的数据转化成01串，添加到codemap中。
                fis.read(codeBytes,0,littleBytes[1]);
                byte[] myCodeBytes=new byte[8*littleBytes[1]-littleBytes[2]];
                int pos=0;
                for(int j=0;j<littleBytes[1]-1;j++){
                    //数组索引小的是二进制中的高位
                    for(int m=7;m>=0;m--){
                        myCodeBytes[pos++]=(byte)(codeBytes[j]>>>m & 1);
                    }
                }
                for(int m=7;m>=littleBytes[2];m--){
                    myCodeBytes[pos++]=(byte)(codeBytes[littleBytes[1]-1]>>>m & 1);
                }
                codeMap.put(littleBytes[0],myCodeBytes);
            }
            //开始通过位流读写器来依次比较编码转化成原有编码。
            System.out.println("2.正在解码压缩数据");
            //创建一个长度为10*1024的缓存
            byte[] bbuf=new byte[10*1024];
            UnCompressByteContainer byteContainer=new UnCompressByteContainer(name,codeMap,last0);
            int hasRead=0;
            long nowLength=0,allLength=0,posLength=fileLength/10;
            while((hasRead=fis.read(bbuf))>0){
                for(int i=0;i<hasRead;i++) {
                    byteContainer.write(bbuf[i]);
                }
                nowLength+=hasRead;
                if(nowLength>posLength){
                    allLength+=nowLength;
                    nowLength=0;
                    System.out.println("进度："+(float)allLength/fileLength);
                }
            }
            byteContainer.close();
            bbuf=null;
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime=System.currentTimeMillis();
        System.out.println(String.format("共用时%d s", (endTime-startTime)/1000));
        System.out.println(String.format("-----压缩完成，得到文件：%s-------", name));
    }
    //生产者消费者模型来生成huffman编码
    private static Map<Byte,byte[]> getCodeMap(String path){
        System.out.println("1.统计字符频率");
        //开启读线程（生产者）
        FileReadThread fileReader=new FileReadThread(path);
        fileReader.start();
        Map<Byte,Long> frequencyMap=new HashMap<>();
        //消费者，读取byte数组频率。
        byte[] bytes;
        while(true){
            try {
                bytes=fileReader.nextBatch();
                //统计频率
                for(int i=0;i<bytes.length;i++) {
                    frequencyMap.put(bytes[i], frequencyMap.getOrDefault(bytes[i], (long) 0) + 1);
                }
                //读完了
                if(bytes.length!=1024){
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //根据频率生成huffman编码
        System.out.println("2.生成huffman编码");
        return getHuffmanCode(frequencyMap);
    }
    //多线程压缩操作
    public static  void mutlComPress(String path) {
        System.out.println("----开始进行多线程压缩操作----");
        long startTime=System.currentTimeMillis();
        File file=new File(path);
        long fileLength=file.length();
        long nowLength=0,allLength=0,posLength=fileLength/10;
        Map<Byte,byte[]> codeMap= getCodeMap(path);
        System.out.println("第一次读文件用时(ms):"+(System.currentTimeMillis()-startTime));
        String outPath="out.huf";
        //将编码内容写入到文件头
        writeHead(outPath,codeMap);
        System.out.println("4.再次读取源文件，开始进行压缩");
        //开启读线程
        FileReadThread fileReader=new FileReadThread(path);
        fileReader.start();

        //开启写线程
        FileWriterThread fileWriter=new FileWriterThread(outPath);
        fileWriter.start();
        //当前线程操作
        MutilComPressByteContainer byteContainer=new MutilComPressByteContainer(fileWriter);
        byte[] bytes;
        while(true){
            try {
                bytes=fileReader.nextBatch();
                nowLength+=bytes.length;
                if(nowLength>posLength){
                    allLength+=nowLength;
                    nowLength=0;
                    System.out.println("进度："+(float)allLength/fileLength);
                }
                //System.out.println("读取源文件"+ Arrays.toString(bytes));
                for(byte tmp:bytes) {
                    //System.out.println(tmp);
                    byteContainer.write(codeMap.get(tmp));
                }
                //读完了
                if(bytes.length!=1024){
                    System.out.print("文件处理完成,");
                    int x=byteContainer.close();
                    try {
                        RandomAccessFile randomFile = new RandomAccessFile(outPath, "rw");
                        randomFile.write(x);
                        System.out.println("补零长度为"+x);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                fileReader.interrupt();
                fileWriter.interrupt();
            }
        }
        long endTime=System.currentTimeMillis();
        System.out.println("压缩总用时(ms)："+(endTime-startTime));
    }
}
