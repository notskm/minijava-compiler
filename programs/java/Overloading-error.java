class NoOverloading {
    public static void main(String[] args) {

    }
}

class A {
    public int function(int a) {
        return a;
    }

    public int function(int a, int b) {
        return b;
    }
}
