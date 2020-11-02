package launcher;

import java.util.ArrayList;

public class TestClass {
    public void log(Object caller, String str) {

    }
    public void mines(int x){
        if(x==0 || x*2<12){
            x++;
        }
        x++;
    }
    public void add(int v){
        int x=1;
        x=x+1;
        int y=v;
        y=y+1;
        if(v>0){
            v++;
        }
        v++;
    }

    public  void bubblesrt(ArrayList<Integer> list)
    {
        Integer temp;
        if (list.size()>1) // check if the number of orders is larger than 1
        {
            for (int x=0; x<list.size(); x++) // bubble sort outer loop
            {
                for (int i=0; i < list.size()-i; i++) {
                    if (list.get(i).compareTo(list.get(i+1)) > 0)
                    {
                        temp = list.get(i);
                        list.set(i,list.get(i+1) );
                        list.set(i+1, temp);
                    }
                }
            }
        }

    }
}
