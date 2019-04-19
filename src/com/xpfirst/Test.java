package com.xpfirst;

public class Test {
    public Test(){
        Integer testI = 0;
        Integer mI = 3;
        if (mI == 1) {
            testI = 1;
        }
        if (mI == 3){
            testI = 2;
        }
        else if (mI == 4)
            testI = 2;

        if (mI==5)
            testI = 6;
        else
            testI = 4;
    }
}
