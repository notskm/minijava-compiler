class NoOverloading {
    public static void main(String[] args) {

    }
}

class A {
    public int function(int a) {
        return a;
    }
}

class B extends A {
    public int function(int a, int b) {
        return a;
    }
}
