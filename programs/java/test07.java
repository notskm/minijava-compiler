class test07 {
    public static void main(String[] a) {
        System.out.println(new Operator().compute());
    }
}

class Operator {
    boolean op1bool;
    boolean op2bool;
    int op1int;
    int op2int;
    boolean result;

    public int compute() {
        int val;
        op1int = 10;
        op2int = 20;
        result = op1int < op2int;
        if (result)
            val = 2;
        else
            val = 1;
        return val;
    }
}
