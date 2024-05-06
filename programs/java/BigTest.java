class BigTest {
    public static void main(String[] args) {
        System.out.println(new Tester().runTests());
    }
}

class Tester {
    int dead1;
    int dead2;

    public int runTests() {
        System.out.println(this.testAssignDeadMemberToDeadMember());
        System.out.println(this.testBranchWithLocalArg());
        return 0;
    }

    public int testAssignDeadMemberToDeadMember() {
        dead1 = dead2;
        return 0;
    }

    public int testBranchWithLocalArg() {
        int a;
        int b;
        int c;
        int d;
        int e;
        int f;
        int g;
        int h;
        int i;
        int j;

        a = 0;
        b = a;
        c = b;
        d = c;
        e = d;
        f = e;
        g = f;
        h = g;
        i = h;
        j = i;
        j = 8;
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);
        System.out.println(e);
        System.out.println(f);
        System.out.println(g);
        System.out.println(h);
        System.out.println(i);

        if (j < 0) {
        } else {
            System.out.println(j);
        }

        return j;
    }
}
