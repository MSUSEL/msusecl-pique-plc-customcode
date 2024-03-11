/**
 * MIT License
 * Copyright (c) 2019 Montana State University Software Engineering Labs
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tool;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Doubles;
import com.opencsv.CSVReader;
import model.RuleFinding;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.FileUtility;
import pique.utility.PiqueProperties;

import utilities.helperFunctions;

import javax.swing.border.LineBorder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class wraps the CODESYS static analysis tool. It initializes
 * the tool, gets the output, and parses that output into a model
 * that can be interpreted in PIQUE.
 */
public class CODESYSWrapper extends Tool implements ITool {
    private static final Logger LOGGER = LoggerFactory.getLogger(CODESYSWrapper.class);

    public CODESYSWrapper() {
        super("CODESYS", null);
    }

    // Methods

    /**
     * @param projectLocation The path to a binary file for the desired solution of project to analyze
     * @return The path to the analysis results file
     */
    @Override
    public Path analyze(Path projectLocation) {
        System.out.println("in project location");

        return projectLocation;
    }

    /**
     * parses output of tool from analyze().
     *
     * @param toolResults location of the results, output by analyze()
     * @return A Map<String,Diagnostic> with findings from the tool attached. Returns null if tool failed to run.
     */
    @Override
    public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
        //toolResults is a directory with 2 files, metrics and rules

        // Add some error handling around checking that tool-output files exist

        System.out.println(this.getName() + " Parsing Analysis...");
        LOGGER.debug(this.getName() + " Parsing Analysis...");

        Pair<Path, Path> benchmarkProjects;
        Path metricsFile = toolResults;
        Path rulesFile = toolResults;
        // loop through every directory in benchmarks
        if (toolResults.toFile().isDirectory()) {
            for(File benchmarkOutputFile : toolResults.toFile().listFiles()) {
                String extension = FileNameUtils.getExtension(benchmarkOutputFile.getName());
                if (extension.equals("csv")) {
                    metricsFile = benchmarkOutputFile.toPath();
                } else if (extension.equals("txt")) {
                    rulesFile = benchmarkOutputFile.toPath();
                } else {
                    LOGGER.debug("Unknown file extension in benchmark repository: " + benchmarkOutputFile.getName());
                    System.out.println("Unknown file extension in benchmark repository: " + benchmarkOutputFile.getName());
                }
            }
        }
        benchmarkProjects = new ImmutablePair<>(metricsFile, rulesFile);

        Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());

        //parse metrics
        Table<String, String, Double> formattedMetricsOutput= parseMetrics(benchmarkProjects.getLeft());

        //parse rules into Findings
        List<List<String>> formattedRulesOutput= parseRules(benchmarkProjects.getRight());
        for (List<String> row : formattedRulesOutput) {
            System.out.println("DiagnosticPrint1: " + row);
            Finding f = new RuleFinding(benchmarkProjects.getRight().toString(), row.get(0), row.get(1), -1);
            System.out.println("Diagnostic print 2: " + row.get(0));
            f.setName(row.get(0));
        }

        return diagnostics;
    }

    /**
     * Initialize CODESYS tool
     */
    @Override
    public Path initialize(Path toolRoot) {
        System.out.println("Initializing");
        // TODO We need to know how to initialize the CODESYS static analysis tool
        return toolRoot;
    }

    /**
     * Parses the metrics output of CODESYS tool
     *
     * @param toolOutput is a path to the directory containing tool output files
     * @return a Table of information to be mapped in parseAnalysis()
     */
    public Table<String, String, Double> parseMetrics(Path toolOutput) {
        int columnDefinitionLines = 2;  // There are two lines at the top of the example output file that define "columns" in the output
        int ignoreLines;
        String fileType;
        String delimiter;
        Table<String, String, Double> formattedToolOuput = HashBasedTable.create();
        String metrics = "";    // This will be the tool output metrics file

        try {
            metrics = helperFunctions.readFileContent(toolOutput);
        } catch (IOException e) {
            LOGGER.info("No results to read from CODESYS.");
        }

        String[] lines = metrics.split("\n");

        fileType = FileNameUtils.getExtension(toolOutput.toString());
        if (fileType.equals(".csv") || fileType.equals(".CSV")) {
            delimiter = ";";
            ignoreLines = 3;
        } else {
            delimiter = "\t";
            ignoreLines = 0;
        }

        // parse lines of output that define columns
        ArrayList<String> columnKeys = new ArrayList<>();

        for (int i = ignoreLines; i < columnDefinitionLines + ignoreLines; i++) {
            String[] columnNames = lines[i].trim().split(delimiter);
            String[] trimmedColumnNames;   // need a better way to do this
            if (Objects.equals(columnNames[0], "")) {
                trimmedColumnNames = Arrays.copyOfRange(columnNames, 1, columnNames.length);
                Collections.addAll(columnKeys, trimmedColumnNames);
            } else {
                Collections.addAll(columnKeys, columnNames);
            }
        }

        // parse rows of values
        int startPoint = ignoreLines + columnDefinitionLines + 1;
        for (int i = startPoint; i < lines.length; i++) {
            ArrayList<String> row = new ArrayList<>();
            String trimmedLine = lines[i].replaceFirst("\\s++\\S", "");
            String[] line = trimmedLine.split(delimiter, -1);
            Collections.addAll(row, line);

            for (int j = 0; j < columnKeys.size(); j++) {
                if (row.get(j + 1).isEmpty()) {
                    // hashBasedTable does not tolerate nulls so replace nulls with -9.9
                    row.set(j + 1, "-9.9");
                }
                formattedToolOuput.put(row.get(0), columnKeys.get(j), Doubles.tryParse(row.get(j + 1)));
            }
        }
        return formattedToolOuput;
    }

    /**
     * Parses the rules output of CODESYS tool
     *
     * @param toolOutput is a path to the directory containing tool output files
     * @return a List of list of strings representing diagnostic information to be mapped in parseAnalysis()
     */
    public List<List<String>> parseRules(Path toolOutput) {
        // Always this filename?
        final String rulesOutput = "static-analysis-output-all_rules_metrics_turnedon.txt";

        String rules = "";
        // Read in tool output
        //example line: [ERROR]         Final Exam: CookieProcess [Device: PLC Logic: Final_Proj](Line 3 (Decl)): SA0033:  Unused Variable 'StartPB'
        try {
            rules = helperFunctions.readFileContent(toolOutput.resolve(rulesOutput));
        } catch (IOException e) {
            LOGGER.info("No results to read from CODESYS.");
        }
        return formatRulesOutput(rules);
    }

    /**
     * Formats the tool output file into a List of lines (themselves represented as ArrayLists of strings)
     * Extraneous detail is removed leaving SA number and description of the vulnerability
     *
     * @param rules A raw String representation of the rules tool output
     * @return formattedOutput An ArrayList containing ArrayLists of strings representing findings associated with a specific standard
     */
    private List<List<String>> formatRulesOutput(String rules) {
        // The number of lines at the top of the file that proved lables rather than static analysis
        // This might only ever say "static analysis" in which case, titleLines can be removed.
        int titleLines = 1;
        List<List<String>> formattedOutput = new ArrayList<>();
        String[] lines = rules.trim().split("\n");

        for(int i = titleLines; i < lines.length - 2; i++) {
            String[] line = lines[i].trim().split(":");
            ArrayList<String> formattedLine = new ArrayList<>();
            // should probably improve performance of this nested loop with more string formatting but this is good enough for now
            for (int j = 0; j < line.length; j++) {
                if (line[j].contains("SA")) {
                    formattedLine.add(line[j].trim());
                    formattedLine.add(line[j + 1].trim());
                }
            }
            formattedOutput.add(formattedLine);
        }
        return formattedOutput;
    }

    /**
     * maps low-critical to numeric values based on the highest value for each range.
     *
     * @param severity a severity score based on industry standards
     * @return the severity score as an Integer object
     */
    private Integer severityToInt(String severity) {
        Integer severityInt = 1;
        switch (severity.toLowerCase()) {
            case "low": {
                severityInt = 1;
                break;
            }
            case "medium": {
                severityInt = 3;
                break;
            }
            case "high": {
                severityInt = 10;
                break;
            }
        }

        return severityInt;
    }
}
