import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.matchers.Null;
import pique.model.Diagnostic;
import pique.model.Finding;
import tool.CODESYSWrapper;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.SQLOutput;
import java.util.*;

import static org.junit.Assert.*;

public class CODESYSWrapperTest {

    protected CODESYSWrapper CODESYSWrapper;

    public CODESYSWrapperTest() {
        CODESYSWrapper = new CODESYSWrapper();

    }

    @BeforeClass
    public static void setup() {
        // Load Properties
       // Path root =
    }

//    @Test
//    public void testIfMetricsAreBeingParsedCorrectly(){
//        Map<String, Diagnostic> diagnostics = CODESYSWrapper.parseAnalysis();
//
//        Diagnostic diagnosticNode = diagnostics.get("Code size (number of bytes)");
//
//
//        assertEquals(diagnostics.get("Code size (number of bytes)").);
//
//    }

    // msusecl-pique-plc-customcode
    @Test
    public void isMetricsOutputParsedToTableCorrectly() {
        double floatDelta = 0.0002; // used in comparing doubles due to inherent precision issues with doubles

        // This is necessary to format the correct path to helperFunctions.readFileContent()
        // and mocking readFileContent() just doesn't make sense here. So this is technically a small integration test
        // metrics-output.txt
        Path relativeFilePath = Paths.get("../msusecl-pique-plc-customcode/src/test/resources/");
        Path inputFile = relativeFilePath.toAbsolutePath().normalize();
        Table<String, String, Double> testTable = CODESYSWrapper.parseMetrics(inputFile);

        // csv-based
        boolean firstRowExists = testTable.containsRow("T_PLC_MS");
        boolean firstColumnExists = testTable.containsColumn("Code size (number of bytes)");
        boolean lastRowExists = testTable.containsRow("BatchCompleteCountFB");
        boolean lastColumnExists = testTable.containsColumn("BIT_COUNT");

        // text-based
//        boolean firstRowExists = testTable.containsRow("SortTable");
//        boolean firstColumnExists = testTable.containsColumn("Code size (number of bytes)");
//        boolean lastRowExists = testTable.containsRow("BatchCompleteCountFB");
//        boolean lastColumnExists = testTable.containsColumn("Number of SFC branches");

        assertTrue(firstRowExists);
        assertTrue(firstColumnExists);
        assertTrue(lastRowExists);
        assertTrue(lastColumnExists);

        // csv-based
        

        // text-based
//        double firstValue = Preconditions.checkNotNull(testTable.get("SortTable", "Code size (number of bytes)")) ;
//        double midValue = Preconditions.checkNotNull(testTable.get("Sim_Piston", "Stack size (number of bytes)"));
//        double lastValue = Preconditions.checkNotNull(testTable.get("BatchCompleteCountFB", "Number of SFC steps"));

        assertEquals(216.0, firstValue, floatDelta);
        assertEquals(16.0, midValue, floatDelta);
        assertEquals(-9.9, lastValue, floatDelta);
    }

    @Test
    public void isRulesOutputParsedCorrectly() {
        Path relativeFilePath = Paths.get("../msusecl-pique-plc-customcode/src/test/resources/");
        Path inputFile = relativeFilePath.toAbsolutePath().normalize();
        List<List<String>> processedFile = CODESYSWrapper.parseRules(inputFile);

        // Does any output exist
        assertFalse(processedFile.isEmpty());
        int filedividedbytwo = processedFile.size() / 2;

        List<String> firstLine = processedFile.get(0);
        List<String> midLine = processedFile.get(98);
        List<String> finalLine = processedFile.get(processedFile.size() - 1);

    // This is not yet reliable
    // There is more string processing to do based on what we care about
    assertEquals(firstLine.get(0), "SA0033");
    assertEquals(midLine.get(0), "SA0162");
    assertEquals(finalLine.get(0), "SA0025");

    assertEquals(firstLine.get(1), "Unused Variable 'StartPB'");
    assertEquals(midLine.get(1), "Missing comment for 'ApplyRed'");
    assertEquals(finalLine.get(1), "Enumeration constant 'Green' not qualified");

    }
}
