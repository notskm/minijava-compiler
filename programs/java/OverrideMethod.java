class OverrideMethod {
    public static void main(String[] args) {

    }
}

class A {
    public int a(int a) {
        return a;
    }
}

class B extends A {
    public int a(int a) {
        return 1;
    }
}
