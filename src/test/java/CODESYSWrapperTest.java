/**
 * MIT License
 *
 * Copyright (c) 2024 Montana State University Software Engineering Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import model.MetricFinding;
import model.RuleFinding;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.matchers.Null;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.model.ModelNode;
import runnable.QualityModelDeriver;
import runnable.SingleProjectEvaluator;
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
    public void isCSVMetricsOutputParsedToTableCorrectly() {
        double floatDelta = 0.0002; // used in comparing doubles due to inherent precision issues with doubles

        // This is necessary to format the correct path to helperFunctions.readFileContent()
        // and mocking readFileContent() just doesn't make sense here. So this is technically a small integration test
        // metrics-output.txt
        Path relativeFilePath = Paths.get("../msusecl-pique-plc-customcode/src/test/resources/MidtermESET_2205_2023-Metrics.csv");
        Path inputFile = relativeFilePath.toAbsolutePath().normalize();
        Table<String, String, Double> testTable = CODESYSWrapper.parseMetrics(inputFile);

        // csv-based
        boolean firstRowExists = testTable.containsRow("T_PLC_MS");
        boolean firstColumnExists = testTable.containsColumn("Code size (number of bytes)");
        boolean lastRowExists = testTable.containsRow("BIT_COUNT");
        boolean lastColumnExists = testTable.containsColumn("Number of SFC steps");

        assertTrue(firstRowExists);
        assertTrue(firstColumnExists);
        assertTrue(lastRowExists);
        assertTrue(lastColumnExists);

        // csv-based
        double firstValue = Preconditions.checkNotNull(testTable.get("T_PLC_MS", "Code size (number of bytes)")) ;
        double midValue = Preconditions.checkNotNull(testTable.get("Main", "Stack size (number of bytes)"));
        double lastValue = Preconditions.checkNotNull(testTable.get("BIT_COUNT", "Number of SFC steps"));

        assertEquals(128.0, firstValue, floatDelta);
        assertEquals(0.0, midValue, floatDelta);
        assertEquals(-9.9, lastValue, floatDelta);

    }

    @Test
    public void isRulesOutputParsedCorrectly() {
        Path relativeFilePath = Paths.get("../msusecl-pique-plc-customcode/src/test/resources/MidtermESET_2205_2023-StaticAnalysisOutput.txt");
        Path inputFile = relativeFilePath.toAbsolutePath().normalize();
        List<List<String>> processedFile = CODESYSWrapper.parseRules(inputFile);

        // Does any output exist
        assertFalse(processedFile.isEmpty());

        List<String> firstLine = processedFile.get(0);
        List<String> midLine = processedFile.get(96);
        List<String> finalLine = processedFile.get(processedFile.size() - 1);

        // This is not yet reliable
        // There is more string processing to do based on what we care about
        assertEquals("SA0031", firstLine.get(0));
        assertEquals("SA0162",midLine.get(0));
        assertEquals("SA0102", finalLine.get(0));

        assertEquals(firstLine.get(1), "Unused Property Get 'Container'");
        assertEquals(midLine.get(1), "Missing comment for 'Z1_button_VI'");
        assertEquals(finalLine.get(1), "Access to program/fb variable 'AlarmTest_VI' from the outside");
    }

//    public void doesInitializeDiagnosticsReturnCorrectNumberOfDiagnostics() {
//        int numberOfDiagnostics = 145;
//
//
//        assertEquals();
//    }

    @Test
    public void areDiagnosticsBuiltCorrectly() {
        // Do diagnostic nodes exist for good input?

        // Do diagnostics conform to correct structure?

        // Assert values in test oracle
    }

    @Test
    public void isModelDefinitionFileValid() {
        QualityModelDeriver qualityModelDeriver = new QualityModelDeriver("src/test/resources/pique-test-properties.properties");
        // assert count of sub-categories of Diagnostics
    }


    @Test
    public void parseAnalysisTest() {
        CODESYSWrapper wrapper = new CODESYSWrapper();
        //wrapper.parseAnalysis(Paths.get("benchmarks/MidtermESET_2205_2023"));
        Map<String, Diagnostic> diagnostics = wrapper.parseAnalysis(Paths.get("benchmarks/WK6HMKComputation_Comparison"));

        assertEquals(9, diagnostics.get("SA0033: Unused variables").getNumChildren());
        assertEquals(4, diagnostics.get("SA0031: Unused signatures").getNumChildren());
        assertEquals(3, diagnostics.get("Code size (number of bytes)").getNumChildren());
        for (ModelNode f : diagnostics.get("Code size (number of bytes)").getChildren().values()){
            if (f.getName().equals("W6PU7_6 - Code size (number of bytes)")){
                assertEquals(new Double(120), ((MetricFinding)f).getMetricValue());
            }
        }
    }

    @Test
    public void acceptanceTest() {
        QualityModelDeriver qualityModelDeriver = new QualityModelDeriver("src/test/resources/pique-test-properties.properties");
    }

    @Test
    public void testEvaluatorFunctionality(){
        SingleProjectEvaluator singleProjectEvaluator = new SingleProjectEvaluator("input/");
    }

}
