class Inheritance {
    public static void main(String[] args) {
        Test tester;
        int throwaway;

        tester = new Test();
        throwaway = tester.testBasicInheritance();
    }
}

class Test {
    public int testBasicInheritance() {
        int throwaway;
        Base test1;
        Derived1 test2;
        Base test3;
        Derived2 test4;
        Base test5;
        Derived1 test6;

        test1 = new Base();
        test2 = new Derived1();
        test3 = test2;
        test4 = new Derived2();
        test5 = test4;
        test6 = test4;

        throwaway = test1.initBase(5);
        System.out.println(test1.add(5)); // out: 10

        throwaway = test2.initBase(3);
        throwaway = test2.initDerived(8);
        System.out.println(test2.add(5)); // out: 8
        System.out.println(test2.sub(5)); // out: 3

        throwaway = test3.initBase(7);
        System.out.println(test2.sub(5)); // out: 3
        System.out.println(test2.add(5)); // out: 12
        System.out.println(test3.add(5)); // out: 12

        throwaway = test4.initBase(4);
        throwaway = test4.initDerived(5);
        throwaway = test4.initDerived2(6);
        System.out.println(test4.add(20)); // out: 40
        System.out.println(test4.sub(20)); // out: 80
        System.out.println(test4.mul(10)); // out: 60
        System.out.println(test5.add(6)); // out: 12
        System.out.println(test6.add(6)); // out: 12

        System.out.println(((new Getter().getDerived2()).add(6))); // out: 12
        return 0;
    }
}

class Getter {
    public Base getDerived2() {
        Derived2 d;
        d = new Derived2();
        return d;
    }
}

class Base {
    int a;

    public int initBase(int b) {
        a = b;
        return 0;
    }

    public int add(int x) {
        return a + x;
    }
}

class Derived1 extends Base {
    int a;

    public int initDerived(int b) {
        a = b;
        return 0;
    }

    public int sub(int x) {
        return a - x;
    }
}

class Derived2 extends Derived1 {
    int a;

    public int initDerived2(int b) {
        a = b;
        return 0;
    }

    public int add(int x) {
        return x + x;
    }

    public int sub(int x) {
        return 100 - x;
    }

    public int mul(int x) {
        return a * x;
    }
}
