package helpbean;

public class Heap {
    private int num;
    private Node[] array;
    private int capacity;
    public Heap(){
        num=0;
        capacity=256;
        array=new Node[capacity];
    }
    public void add(Node node){
        int i=num++;
        while (i>0&&node.frequency<array[(i-1)/2].frequency){
            array[i]=array[(i-1)/2];
            i=(i-1)/2;
        }
        array[i]=node;
    }
    public  Node poll(){
        Node result=array[0];
        array[0]=array[--num];
        percolateDown(0);
        return result;
    }
    public void clear(){
        this.num=0;
        array=null;
    }

    public int getNum() {
        return num;
    }

    private void percolateDown(int i){
        int l,r,min;
        Node tmpNode;
        l=i*2+1;
        r=i*2+2;
        if(l<num&&array[i].frequency>array[l].frequency){
            min=l;
        }
        else
            min=i;
        if(r<num&&array[min].frequency>array[r].frequency){
            min = r;
        }
        if(min!=i){
            tmpNode=array[i];
            array[i]=array[min];
            array[min]=tmpNode;
            percolateDown(min);
        }
    }
}