package helpbean;

public class Node{
    public Node left,right;
    public byte myByte;
    public long frequency;
    public Node(byte myByte,long frequency){
        this.myByte=myByte;
        this.frequency=frequency;
    }
    public Node(long frequency,Node left,Node right){
        this.frequency=frequency;
        this.left=left;
        this.right=right;
    }
}

