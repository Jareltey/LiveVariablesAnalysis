package common;

import soot.*;
import soot.tagkit.Host;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    private static HashSet<ErrorReport> errors = new HashSet<>();

    /** Converts a Unit from a given Body to a String */
    public static String toString(Unit unit, Body body) {
        NormalUnitPrinter printer = new NormalUnitPrinter(body);
        unit.toString(printer);
        return printer.output().toString();
    }

    /**
     * Reports a warning message for the given element of source code,
     * using the ErrorMessage passed in. The ErrorMessage, the name of
     * the element if it is a declaration, and the line number are stored
     * for later lookup
     */
    public static void reportWarning(Host element, ErrorMessage message) {
        System.out.print("warning: ");
        System.out.print(message.getErrorMessage());
        int line;
        if (element == null)
            line = -1;
        else
            line = element.getJavaSourceStartLineNumber();
        String name = getName(element);
        if (line == -1) {
            if (name != null) {
                System.out.println(" at the declaration of" + name);
            } else {
                System.out.println(" (line unknown)");
            }
        } else {
            System.out.print(" at line ");
            System.out.println(line);
        }
        if (message == ErrorMessage.VARIABLE_DEFINITION_ERROR || message == ErrorMessage.VARIABLE_REFERENCED_ERROR) {
            errors.add(new ErrorReport(message, line, name));
        }
        if (message == ErrorMessage.VARIABLE_DEFINITION_WARNING || message == ErrorMessage.VARIABLE_REFERENCED_WARNING) {
            errors.add(new ErrorReport(message, line, name));
        }

    }

    public static void resetErrors() {
        errors = new HashSet<>();
    }

    public static Set<ErrorReport> getErrors() {
        return errors;
    }

    /**
     * Returns the name of the element.
     * @param element target element
     * @return name
     */
    private static String getName(Host element) {
        if (element instanceof SootClass) {
            return ((SootClass) element).getName();
        } else if (element instanceof SootField) {
            return ((SootField) element).getName();
        } else if (element instanceof SootMethod) {
            return ((SootMethod) element).getName();
        } else {
            return null;
        }
    }
}
