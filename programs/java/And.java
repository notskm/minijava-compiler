class And {
    public static void main(String[] args) {
        boolean a;
        boolean b;

        a = true;
        b = true;
        if (a && b) {
            System.out.println(1);
        } else {
            System.out.println(0);
        }

        a = false;
        b = true;
        if (a && b) {
            System.out.println(0);
        } else {
            System.out.println(1);
        }

        a = true;
        b = false;
        if (a && b) {
            System.out.println(0);
        } else {
            System.out.println(1);
        }

        a = false;
        b = false;
        if (a && b) {
            System.out.println(0);
        } else {
            System.out.println(1);
        }
    }
}
