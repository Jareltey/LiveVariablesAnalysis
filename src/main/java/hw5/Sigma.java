package hw5;

import soot.Local;
import soot.SootMethod;
import soot.toolkits.scalar.Pair;

import java.lang.*;
import java.util.HashMap;
import java.util.*;
/**
 * A class to represent abstract values at a program point.
 */
public class Sigma {
    /**
     * Elements of lattice are subsets of VAR, where WAR is set of all local variables in program
     */

    public Set<Local> live_variables;

    /**
     * An empty sigma
     */
    public Sigma() {
        this.live_variables = new HashSet<>();
    }

    /**
     * An initialized sigma
     * @param locals live variables at this point
     */
    public Sigma(Iterable<Local> locals) {
        this.live_variables = new HashSet<>();
        for (Local l : locals) {
            this.live_variables.add(l);
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder("{ ");
        for (Local var : this.live_variables) {
            str.append(var).append(",");
        }
        return str + " }";
    }

    public void copy(Sigma dest) {
        dest.live_variables = new HashSet<>(live_variables);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {return false;}
        if (this == obj) {return true;}
        return (obj instanceof Sigma) && (this.live_variables.equals(((Sigma) obj).live_variables));
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}

