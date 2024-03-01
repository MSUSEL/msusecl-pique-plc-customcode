import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import org.junit.BeforeClass;
import org.junit.Test;
import pique.model.Diagnostic;
import pique.model.Finding;
import tool.CODESysWrapper;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class CODESysWrapperTest {

    protected CODESysWrapper codeSysWrapper;

    public CODESysWrapperTest() {
        codeSysWrapper = new CODESysWrapper();

    }

    @BeforeClass
    public static void setup() {
        // Load Properties
       // Path root =
    }

//    @Test
//    public void testIfMetricsAreBeingParsedCorrectly(){
//        Map<String, Diagnostic> diagnostics = codeSysWrapper.parseAnalysis();
//
//        Diagnostic diagnosticNode = diagnostics.get("Code size (number of bytes)");
//
//
//        assertEquals(diagnostics.get("Code size (number of bytes)").);
//
//    }

    // msusecl-pique-plc-customcode
    @Test
    public void testIfMetricsOutputIsParsedToTableCorrectly() {
        double floatDelta = 0.0002;
        // This is necessary to format the correct path to helperFunctions.readFileContent()
        // and mocking readFileContent() just doesn't make sense here. So this is technically an integration test
        Path relativeFilePath = Paths.get("../msusecl-pique-plc-customcode/src/test/resources/metrics-output.txt");
        Path inputFile = relativeFilePath.toAbsolutePath().normalize();
        System.out.println(inputFile.toString());
        Table<String, String, Double> testTable = codeSysWrapper.parseMetrics(inputFile);

        boolean firstRowExists = testTable.containsRow("SortTable");
        boolean firstColumnExists = testTable.containsColumn("Code size (number of bytes)");
        boolean lastRowExists = testTable.containsRow("BatchCompleteCountFB");
        boolean lastColumnExists = testTable.containsColumn("Number of SFC branches");

        assertTrue(firstRowExists);
        assertTrue(firstColumnExists);
        assertTrue(lastRowExists);
        assertTrue(lastColumnExists);


        double firstValue = testTable.get("SortTable", "Code size (number of bytes)");
        double midValue = testTable.get("Sim_Piston", "Stack size (number of bytes)");
        double lastValue = testTable.get("BatchCompleteCountFB", "Number of SFC branches");


        Set<String> columnKeySet = testTable.columnKeySet();
        System.out.println(columnKeySet);
        System.out.println(testTable.column("Number of SFC branches"));

        assertEquals(216.0, firstValue, floatDelta);
        assertEquals(16.0, midValue, floatDelta);
        assertEquals(-9.9, lastValue, floatDelta);
    }




}
