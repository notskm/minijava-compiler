class test03 {
    public static void main(String[] a) {
        System.out.println(new Operator().compute());
    }
}

class Operator {
    int result;

    public int compute() {
        result = 10 + 20; // try with 10*20 also
        return result;
    }
}
