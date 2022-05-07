package hw5;

import common.ErrorMessage;
import common.Utils;
import org.junit.Assert;
import org.junit.Test;
import soot.Main;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.options.Options;
import sun.jvm.hotspot.opto.Phase;

public class IntraAnalysisTest extends AnalysisTest {
    void add_analysis() {
        analysisName = IntraAnalysisTransformer.ANALYSIS_NAME;
        PackManager.v().getPack("jap").add(
                new Transform(analysisName,
                        IntraAnalysisTransformer.getInstance())
        );
        PhaseOptions.v().setPhaseOption("jb.ule", "enabled:false");
    }

    @Test
    public void testIntraAnalysis() {
        addTestClass("inputs.IntraTest");
        Main.main(getArgs());

        // VARIABLE_DEFINITION_WARNING -> appears once for each statement that defines a variable
        // VARIABLE_DEFINITION_ERROR -> did not kill variable defined in sigma_out (wrong behaviour)
        // VARIABLE_REFERENCED_WARNING -> appears once for each statement that references variable(s)
        // VARIABLE_REFERENCED_ERROR -> did not gen variable(s) referenced in sigma_out (wrong behaviour)
        // Thus we should expect many warnings (due to multiple definitions/references but no errors)

        // test01
        addExpected(ErrorMessage.VARIABLE_DEFINITION_WARNING, 8);
        addExpected(ErrorMessage.VARIABLE_DEFINITION_WARNING, 11);
        addExpected(ErrorMessage.VARIABLE_DEFINITION_WARNING, 12);
        addExpected(ErrorMessage.VARIABLE_DEFINITION_WARNING, 13);
        addExpected(ErrorMessage.VARIABLE_DEFINITION_WARNING, 14);
        addExpected(ErrorMessage.VARIABLE_DEFINITION_WARNING, 15);

        addExpected(ErrorMessage.VARIABLE_REFERENCED_WARNING, 12);
        addExpected(ErrorMessage.VARIABLE_REFERENCED_WARNING, 13);
        addExpected(ErrorMessage.VARIABLE_REFERENCED_WARNING, 14);
        addExpected(ErrorMessage.VARIABLE_REFERENCED_WARNING, 15);
        addExpected(ErrorMessage.VARIABLE_REFERENCED_WARNING, 16);

        Assert.assertEquals(expected, Utils.getErrors());
    }
}
