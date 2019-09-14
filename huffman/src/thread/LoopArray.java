package thread;

public class LoopArray {
    private int x=0;
    private byte[][] array=new byte[30][1024];
    public byte[] get(){
        x=(x+1)%10;
        return array[x];
    }
}
