package inputs;

public class IntraTest {

    public static int test1() {
        int a,b,c,d,e;
        int ignore;
        int[] array = new int[5]; // sigma_out live variables -> {}
        a = 5; // optimized away by copy propagation
        b = 2; // optimized away by copy propagation
        c = a - b; // sigma_out live variables -> {array} (copy propagation replaces a,b with constants)
        ignore = array[(int)c]; // sigma_out live variables -> {array,c} (copy propagation replaces b with constant)
        d = b + c; // sigma_out live variables -> {array,c} (copy propagation replaces b with constant)
        ignore = array[(int)d]; // sigma_out live variables -> {array,c,d}
        e = d - c; // sigma_out live variables -> {c,d}
        return e; // sigma_out live variables -> {e}
    }

//    public static void test2() {
//        double a,b,c,d,e;
//        int ignore;
//        int[] array = new int[7];
//        a = 1.0; // a -> [1.0,1.0]
//        b = 2.0; // b -> [2.0,2.0]
//        if (a != b) {
//            a += 1.0; // a -> [2.0,2.0]
//        } else {
//            a -= 1.0; // a -> [0.0,0.0]
//        } // a -> [0.0,2.0]
//        ignore = array[(int)a]; // OK
//        if (b != a) {
//            b *= 2; // b -> [4.0,4.0]
//        } else {
//            b /= 2; // b -> [1.0,1.0]
//        } // b -> [1.0,4.0]
//        ignore = array[(int)b]; // OK
//        c = a + b; // c -> [1.0,6.0]
//        ignore = array[(int)c]; // OK
//        d = b - a; // d -> [-1.0,4.0]
//        ignore = array[(int)d]; // WARNING
//        e = a * b; // e -> [0.0,8.0]
//        ignore = array[(int)e]; // WARNING
//    }

//    public static void test3() {
//        double a,b,c;
//        int ignore;
//        int[] array = new int[5];
//        a = getInt(); // a -> [-∞,+∞]
//        b = 1.0; // b -> [1.0,1.0]
//        c = a / b; // c -> [-∞,+∞]
//        ignore = array[(int)c]; // WARNING
//    }



    public static int getInt() {
        return 0;
    }

    public static boolean condition() {
        return true;
    }
}
