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

    public static int test2() {
        int a,b,c,d;
        int ignore;
        int[] array = new int[7]; // sigma_out live variables -> {}
        a = 1; // optimized away by copy propagation
        b = 2; // optimized away by copy propagation
        c = b - a; // sigma_out live variables -> {array} (copy propagation replaces a,b with constants)
        d = a - b; // sigma_out live variables -> {array,c} (copy propagation replaces a,b with constants)
        if (d != c) { // sigma_out live variables -> {array,c,d}
            c += 1; // sigma_out live variables -> {array,c} (both define and reference c)
        } else {
            c -= 1; // sigma_out live variables -> {array,c} (both define and reference c)
        }
        ignore = array[(int)a]; // sigma_out live variables -> {array,c} (copy propagation replaces a with constant)
        return c; // sigma_out live variables -> {c}
    }

}
