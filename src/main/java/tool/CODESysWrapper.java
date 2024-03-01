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
 *
 *
 */
public class CODESysWrapper extends Tool implements ITool {
    private static final Logger LOGGER = LoggerFactory.getLogger(CODESysWrapper.class);

    public CODESysWrapper() {
        super("CODESys", null);
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
            LOGGER.debug("Error creating directory to save CODESys tool results");
            System.out.println("Error creating directory to save CODESys tool results");
        }
        // Do we want this to be formatted as json?
        File tempResults = new File(workingDirectoryPrefix + "codesys-" + imageName + ".json");
        if (tempResults.exists()) {
            LOGGER.info("Already ran CODESys on: " + imageName + ", results stored in: " + tempResults.toString());
        } else {
            LOGGER.info("Have not run CODESys on: " + imageName + ", running now and storing in:" + tempResults.toString());
            tempResults.getParentFile().mkdirs();
            //Unlike Grype, Trivy does not automatically download an image if it doesn't already exist.
            //so, we need to download it.

            //TODO determine how CODESys handles this compared to Gype/Trivy
            //TODO run CodeSys and put both metrics and rules file in same directory
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
        // Return the parent directory of CodeSys output as a Path
        // This lets us run parseAnalysis on each file without changing our interface
        return Paths.get(tempResults.getParent());
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
        //TODO Map JSONified results

        //parse rules
        Table<String, String, String> formattedRulesOutput= parseRules(toolResults);
        //TODO Map JSONified results


        // The following instantiation has dummy values for parameters to clarify function for exploration of pique
        //Finding f = new Finding(toolResults.toString(), lineNumber, 1, 1); //getSeverityFromModel);

        Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());

        return diagnostics;
    }

    /**
     * Initializes the tool by installing it through python pip from the command line.
     */
    @Override
    public Path initialize(Path toolRoot) {
        System.out.println("Initializing");
//        final String[] cmd = {"trivy", "version"};
//
//        try {
//            helperFunctions.getOutputFromProgram(cmd, LOGGER);
//        } catch (IOException e) {
//            e.printStackTrace();
//            LOGGER.error("Failed to initialize " + this.getName());
//            LOGGER.error(e.getStackTrace().toString());
//        }
//
        return toolRoot;
    }

    /*
     * Parses the metrics output of CODESys tool
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
        // get the full file path
        Path metricsFilePath = toolOutput;

        // This might be better as a map but I'm not sure how or if metrics and rules interact
        // This way, parsed json can be mapped later
        try {
            metrics = helperFunctions.readFileContent(metricsFilePath);
        } catch (IOException e) {
            LOGGER.info("No results to read from CODESys.");
        }

        // Maybe wrap this in try-catch? What error to catch?
        String[] lines = metrics.split("\n");


        // Parse first two lines of output that define columns
        //ArrayList<String> columnKeys = new ArrayList<>();
//        for (int i = 0; i < columnDefinitionLines; i++) {
//            String[] columnNames = lines[i].trim().split("\t");
//            columnKeys.addAll(Arrays.asList(columnNames));
//        }
        String[] columnNames = lines[0].trim().split("\t");
        ArrayList<String> columnKeys = new ArrayList<>(Arrays.asList(columnNames));

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
                formattedToolOuput.put(rowKeys.get(iter), columnKeys.get(j), Doubles.tryParse(formattedLine.get(j)));
            }
            iter++;
        }

//        for (Integer value : )

        return formattedToolOuput;
    }

    /*
     * Parses the rules output of CODESys tool
     *
     * @param toolOutput is a path to the directory containing tool output files
     * @return an JSONObject of diagnostic(finding?) information to be mapped in parseAnalysis()
     */
    private Table<String, String, String> parseRules(Path toolOutput) {
        // Always this filename?
        final String rulesOutput = "static-analysis-output-all_rules_metrics_turnedon.txt";

        Table<String, String, String> formattedOutput = HashBasedTable.create();
        //example: [ERROR]         Final Exam: CookieProcess [Device: PLC Logic: Final_Proj](Line 3 (Decl)): SA0033:  Unused Variable 'StartPB'
        int lineNumber = 0;//PARSE ME FROM FILE

        return formattedOutput;
    }

    private ArrayList<String> metricsLineBuilder(StringBuilder value, StringBuilder unformattedLine, ArrayList<String> line, boolean secondTab) {
        // base cases
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
                line.add("-9.9");
                return metricsLineBuilder(value, unformattedLine.deleteCharAt(0), line, true);
            }
        }
    }

    //maps low-critical to numeric values based on the highest value for each range.
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
