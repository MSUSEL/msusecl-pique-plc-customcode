import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Diagnostic;
import pique.model.Finding;
import tool.CODESysWrapper;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CODESysWrapperTest {

    protected CODESysWrapper codeSysWrapper;

    public CODESysWrapperTest() {
        codeSysWrapper = new CODESysWrapper();

    }

    @BeforeClass
    public static void setup() {
        // Load Properties
    }

    @Test
    public void testIfMetricsAreBeingParsedCorrectly(){
        Map<String, Diagnostic> diagnostics = codeSysWrapper.parseAnalysis();

        Diagnostic diagnosticNode = diagnostics.get("Code size (number of bytes)");


        assertEquals(diagnostics.get("Code size (number of bytes)").);

    }




}
