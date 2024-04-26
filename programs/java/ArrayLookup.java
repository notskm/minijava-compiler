class ArrayLookup {
    public static void main(String[] args) {
        Implementation impl;
        impl = new Implementation();
        System.out.println(impl.testLookupWithIntLiteral());
        System.out.println(impl.testLookupWithVariable());
        System.out.println(impl.testLookupWithIntInParens());
        System.out.println(impl.testLookupWithVariableInParens());
        System.out.println(impl.testLookupWithLookupInParens());
        System.out.println(impl.testLookupFromMethodCall());
    }
}

class Implementation {
    int[] arr;

    public int testLookupWithIntLiteral() {
        int one;

        arr = new int[5];
        arr[1] = 1;
        one = arr[1];
        System.out.println(one);

        return 0;
    }

    public int testLookupWithVariable() {
        int one;
        int result;

        one = 1;
        arr = new int[5];
        arr[1] = 2;

        result = arr[one];
        System.out.println(result);

        return 0;
    }

    public int testLookupWithIntInParens() {
        int result;

        arr = new int[5];
        arr[1] = 3;

        result = arr[(1)];
        System.out.println(result);

        return 0;
    }

    public int testLookupWithVariableInParens() {
        int one;
        int result;

        one = 1;
        arr = new int[5];
        arr[1] = 4;

        result = arr[(one)];
        System.out.println(result);

        return 0;
    }

    public int testLookupWithLookupInParens() {
        int result;
        int[] arr1;

        arr = new int[5];
        arr1 = new int[5];

        arr1[4] = 3;
        arr[3] = 5;

        result = arr[(arr1[4])];
        System.out.println(result);

        return 0;
    }

    public int testLookupFromMethodCall() {
        int result;

        result = (this.returnArray(6))[0];
        System.out.println(result);

        return 0;
    }

    public int[] returnArray(int val) {
        int[] ret;
        ret = new int[5];
        ret[0] = val;
        return ret;
    }
}
