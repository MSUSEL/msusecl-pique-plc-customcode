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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.utility.PiqueProperties;

import utilities.helperFunctions;

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
 *
 * IMPORTANT regarding metrics output!!!
 * There are currently some problems in this class. Parsing the metrics file
 * is not straightforward and the following questions need to be answered before
 * we can have confidence in our model.
 *
 * How general is our metrics-output.txt oracle? If it is not representative
 * of all possible CODESYS output, then parseAnalysis() is much too fragile
 * to handle the general case.
 *
 * Why does metrics-output.txt not include values for the last column? In the
 * output, that column name is on a separate line from the rest.
 *
 * Do the metrics ever output negative values? The Table data structure being
 * used cannot accept null or empty values, and 0 is in the range of possible
 * output values. Therefore, negative numbers are used to represent nulls in the
 * intermediate step between tool-output file, and PIQUE model.
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
        String imageName = projectLocation.toString();
        LOGGER.info(this.getName() + "  Analyzing " + imageName);
        System.out.println("Analyzing " + imageName + " with " + this.getName());
        String imageNameForDirectory = imageName.split(":")[0];

        //set up results dir
        String workingDirectoryPrefix = "";
        try {
            //read output dir from properties file. FIXME we need better properties import
            Properties prop = PiqueProperties.getProperties("src/main/resources/pique-properties.properties");
            Path resultsDir = Paths.get(prop.getProperty("results.directory"));

            workingDirectoryPrefix = resultsDir + "/tool-out/" + imageNameForDirectory + "/";
            Files.createDirectories(Paths.get(workingDirectoryPrefix));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            LOGGER.debug("Error creating directory to save CODESYS tool results");
            System.out.println("Error creating directory to save CODESYS tool results");
        }
        // Do we want this to be formatted as json?
        File tempResults = new File(workingDirectoryPrefix + "CODESYS-" + imageName + ".json");
        if (tempResults.exists()) {
            LOGGER.info("Already ran CODESYS on: " + imageName + ", results stored in: " + tempResults.toString());
        } else {
            LOGGER.info("Have not run CODESYS on: " + imageName + ", running now and storing in:" + tempResults.toString());
            tempResults.getParentFile().mkdirs();
            //Unlike Grype, Trivy does not automatically download an image if it doesn't already exist.
            //so, we need to download it.

            //TODO determine how CODESYS handles this compared to Gype/Trivy
            //TODO run CODESYS and put both metrics and rules file in same directory
//                 String[] cmd = {"trivy",
//                         "image",
//                         "--format", "json",
//                         "--quiet",
//                         "--output", tempResults.toPath().toAbsolutePath().toString(),
//                         projectLocation.toString()};
//                 LOGGER.info(Arrays.toString(cmd));
//                 try {
//                     helperFunctions.getOutputFromProgram(cmd, LOGGER);
//                 } catch (IOException e) {
//                     LOGGER.error("Failed to run Trivy");
//                     LOGGER.error(e.toString());
//                     e.printStackTrace();
//                 }
        }
        // Return the parent directory of CODESYS output as a Path
        // This lets us run parseAnalysis on each file without changing our interface
        return Paths.get(tempResults.getPath());
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

        System.out.println(this.getName() + " Parsing Analysis...");
        LOGGER.debug(this.getName() + " Parsing Analysis...");

        //parse metrics
        Table<String, String, Double> formattedMetricsOutput= parseMetrics(toolResults);

        //parse rules
        List<List<String>> formattedRulesOutput= parseRules(toolResults);

        //Finding f = new Finding(toolResults.toString(), lineNumber, 1, 1); //getSeverityFromModel);
        // TODO Parse findings and map to diagnostic

        Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());

        return diagnostics;
    }

    /**
     * Initialize CODSYS tool
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
     * @return an JSONObject of diagnostic(finding?) information to be mapped in parseAnalysis()
     */
    public Table<String, String, Double> parseMetrics(Path toolOutput) {
        // Will these filenames always be the same or do we need a cleverer way to grab each file?
        final String metricsOutput = "metrics-output.txt";
        int columnDefinitionLines = 2;  // There are two lines at the top of the example output file that define "columns" in the output
        ArrayList<String> rowKeys = new ArrayList<>();

        String metrics = "";    // This will be the tool output metrics file

        try {
            metrics = helperFunctions.readFileContent(toolOutput.resolve(metricsOutput));
        } catch (IOException e) {
            LOGGER.info("No results to read from CODESYS.");
        }
        String[] lines = metrics.split("\n");

        // parse first two lines of output that define columns
        ArrayList<String> columnKeys = new ArrayList<>();
        for (int i = 0; i < columnDefinitionLines; i++) {
            String[] columnNames = lines[i].trim().split("\t");
            columnKeys.addAll(Arrays.asList(columnNames));
        }

        Table<String, String, Double> formattedToolOuput = HashBasedTable.create();
        int iter = 0;
        for (int i = columnDefinitionLines + 1; i < lines.length; i++) {
            // create parameters necessary for line parsing
            ArrayList<String> line = new ArrayList<>();
            StringBuilder unformattedLine = new StringBuilder(lines[i]);
            StringBuilder value = new StringBuilder();

            ArrayList<String> formattedLine = metricsLineBuilder(value, unformattedLine, line, false);
            rowKeys.add(formattedLine.get(0));
            formattedLine.remove(0);
            for (int j = 0; j < columnKeys.size(); j++) {
                // IMPORTANT!!!
                // This first if statement is very, very bad. Remove it as soon as possible!!
                // We're missing values for the last column. It isn't parsed wrong - the values don't exist in the output file
                // this leads to a lack of confidence in any of the findings because we have more metrics than values
                // and there is no proof that the rest of the values are aligned to the correct columns.
                if (j == 28) {
                    formattedToolOuput.put(rowKeys.get(iter), columnKeys.get(j), Doubles.tryParse(formattedLine.get(j - 1)));
                }
                else {
                    formattedToolOuput.put(rowKeys.get(iter), columnKeys.get(j), Doubles.tryParse(formattedLine.get(j)));
                }
            }
            iter++;
        }
        return formattedToolOuput;
    }

    /**
     * Parses the rules output of CODESYS tool
     *
     * Questions:
     * What information from this file do we actually use?
     * Every line contains "[ERROR]", "Final Exam:", and "[Device: PLC Logic: Final_Proj]"
     * Do these values ever change - eg [WAARNING]? or [Device: <name>: <run name>?"
     * First version of this method includes all parts of the line in case there are other options
     *
     * @param toolOutput is a path to the directory containing tool output files
     * @return an JSONObject of diagnostic(finding?) information to be mapped in parseAnalysis()
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
     * Formats the read-in tool output file into a List of lines (themselves represented as ArrayLists of strings)
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
     * Recursively parses raw lines from the metrics output file. The main purpose is to create an ArrayList that contains
     * a row label followed by all values in the row as separate elements of the ArrayList. This ArrayList can then be used
     * to build the rows of the Table data structure. What makes this level of processing necessary is that tab characters represent
     * both the delimiter and empty/null values
     *
     * @param value StringBuilder that is used to store the values in a line of output as differentiated from delimiters or labels
     * @param unformattedLine raw line in string form that is parsed into a formatted line
     * @param line formatted line and the output of this method
     * @param secondTab flag used to determine whether a tab character is a delimiter or an empty value in the line
     * @return line ArrayList of the row label followed by each value (or null represented by -9.9) in the row
     */
    private ArrayList<String> metricsLineBuilder(StringBuilder value, StringBuilder unformattedLine, ArrayList<String> line, boolean secondTab) {
        // base case
        if (unformattedLine.length() == 0) {
            // return finished line
            return line;
        } else {
            // current character is not a tab, add to value, call lineBuilder with modified unformatted line
            if (unformattedLine.charAt(0) != '\t') {
                value.append(unformattedLine.charAt(0));
                return metricsLineBuilder(value, unformattedLine.deleteCharAt(0), line, false);
            // current character is a first tab, set secondTab to true (treat this tab as a delimiter), store current value in line, and make recursive call to lineBuilder
            } else if (!secondTab) {
                if (value.length() != 0) {
                    line.add(value.toString());
                }
                value.setLength(0);
                return metricsLineBuilder(value, unformattedLine.deleteCharAt(0), line, true);
            // current character is the second tab in a row. Treat it as a null value
            } else {
                // This is weird, but I need a way to represent a null value that doesn't use null, 0.0, or empty string
                // due to the constraints of guava Tables
                line.add("-9.9");
                return metricsLineBuilder(value, unformattedLine.deleteCharAt(0), line, true);
            }
        }
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
                severityInt = 4;
                break;
            }
            case "medium": {
                severityInt = 7;
                break;
            }
            case "high": {
                severityInt = 9;
                break;
            }
            case "critical": {
                severityInt = 10;
                break;
            }
        }

        return severityInt;
    }
}
