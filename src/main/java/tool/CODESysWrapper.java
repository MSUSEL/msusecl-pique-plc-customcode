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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import pique.model.Finding;
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
        JSONObject JSONmetrics = parseMetricsToJSON(toolResults);
        //TODO Map JSONified results

        //parse rules
        JSONObject JSONrules = parseRulestoJSON(toolResults);
        //TODO Map JSONified results


        // The following instantiation has dummy values for parameters to clarify function for exploration of pique
        Finding f = new Finding(toolResults.toString(), lineNumber, 1, 1); //getSeverityFromModel);

        Map<String, Diagnostic> diagnostics = helperFunctions.initializeDiagnostics(this.getName());


        String rules = "";


        try {
            metrics = helperFunctions.readFileContent(toolResults);
        } catch (IOException e) {
            LOGGER.info("No results to read from CODESys.");
        }

        try {
            // if the results field is null we had no findings, thus return
            if (results.isEmpty()) {
                return diagnostics;
            }

            String[] lines = results.split("\n");

            for (int i = 0; i < results.length(); i++) {

            }

//                 for (int i = 0; i < trivyResults.length(); i++) {
//                     JSONArray jsonVulnerabilities = ((JSONObject) trivyResults.get(i)).optJSONArray("Vulnerabilities");
//                     if (jsonVulnerabilities != null){
//                         //apparently is null when no vulnerabilities are found.
//                         for (int j = 0; j < jsonVulnerabilities.length(); j++) {
//                             JSONObject jsonFinding = ((JSONObject) jsonVulnerabilities.get(j));
//                             String vulnerabilityID = jsonFinding.getString("VulnerabilityID");
//
//                             ArrayList<String> associatedCWEs = new ArrayList<>();
//                             JSONArray jsonWeaknesses = jsonFinding.optJSONArray("CweIDs");
//                             if (jsonWeaknesses == null) {
//                                 //try the cve-cwe script...
//                                 ArrayList<String> wrapper = new ArrayList<>();
//                                 wrapper.add(vulnerabilityID);
//                             }else {
//                                 for (int k = 0; k < jsonWeaknesses.length(); k++) {
//                                     associatedCWEs.add(jsonWeaknesses.get(k).toString());
//                                 }
//                             }
//                             //regardless of cwes, continue with severity.
//                             String vulnerabilitySeverity = jsonFinding.getString("Severity");
//                             int severity = this.severityToInt(vulnerabilitySeverity);
//
//                             for (int k = 0; k < associatedCWEs.size(); k++) {
//                                 Diagnostic diag = diagnostics.get((associatedCWEs.get(k) + " Diagnostic Trivy"));
//                                 if (diag != null) {
//                                     Finding finding = new Finding("", 0, 0, severity);
//                                     //finding.setName(vulnerabilityID);
//                                     diag.setChild(finding);
//                                 } else {
//                                     //this means that either it is unknown, mapped to a CWE outside of the expected results, or is not assigned a CWE
//                                     // We may want to treat this in another way in the future, but im ignoring it for now.
//                                     System.out.println("Vulnerability " + vulnerabilityID + " with CWE: " + associatedCWEs.get(k) + "  outside of CWE-1000 was found. Ignoring this CVE.");
//                                     LOGGER.warn("Vulnerability " + vulnerabilityID + " with CWE: " + associatedCWEs.get(k) + "  outside of CWE-1000 was found. Ignoring this CVE.");
//                                 }
//                             }
//                         }
//                     }
//                 }

        } catch (JSONException e) {
            LOGGER.warn("Unable to read results from Trivy JSON output");
        }

        return diagnostics;
    }

    /**
     * Initializes the tool by installing it through python pip from the command line.
     */
    @Override
    public Path initialize(Path toolRoot) {
        final String[] cmd = {"trivy", "version"};

        try {
            helperFunctions.getOutputFromProgram(cmd, LOGGER);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Failed to initialize " + this.getName());
            LOGGER.error(e.getStackTrace().toString());
        }

        return toolRoot;
    }

    /*
     * Parses the metrics output of CODESys tool
     *
     * @param toolOutput is a path to the directory containing tool output files
     * @return an JSONObject of diagnostic(finding?) information to be mapped in parseAnalysis()
     */
    private JSONObject parseMetricsToJSON(Path toolOutput) {
        // Will these filenames always be the same or do we need a cleverer way to grab each file?
        final String metricsOutput = "metrics-output.txt";
        int columnDefinitionLines = 2;  // There are two lines at the top of the example output file that define "columns" in the output

        JSONObject jsonOutput = new JSONObject();
        String metrics = "";
        ArrayList<String> columns = new ArrayList<>();

        // get the full file path
        Path metricsFilePath = toolOutput.resolve(metricsOutput);

        // This might be better as a map but I'm not sure how or if metrics and rules interact
        // This way, parsed json can be mapped later
        try {
            metrics = helperFunctions.readFileContent(metricsFilePath);
        } catch (IOException e) {
            LOGGER.info("No results to read from CODESys.");
        }

        try {
            String[] lines = metrics.split("\n");

            // Parse first two lines of output that define columns
            for (int i = 0; i < columnDefinitionLines; i++) {
                String[] columnNames = lines[i].trim().split("\t");
                columns.addAll(Arrays.asList(columnNames));
            }

            // Initialize a json object which represents a row of output
            // the row items are stored as a json array of { column name : value } json objects
            JSONObject jsonRowlabel = new JSONObject();
            JSONArray jsonRowBody = new JSONArray();

            // Parse regular lines to match up with column names
            // start after the first few lines that define the column names
            // Example jsonified line: {"SortTable" : [ {"Code size (number of bytes)" : 216}, {"Variables size (number of bytes)" : 88}...] }
            for (int i = columnDefinitionLines + 1; i < lines.length; i++) {
                String[] rowItems = lines[i].trim().split("\t");


                jsonOutput.put(rowItems[0],)
                for (String item : rowItems) {
                    jsonOutput.put(item, )
                }

            }

        } catch (JSONException e) {
            LOGGER.warn("Unable to read results from Trivy JSON output");
        }
        return jsonOutput;
    }

    /*
     * Parses the rules output of CODESys tool
     *
     * @param toolOutput is a path to the directory containing tool output files
     * @return an JSONObject of diagnostic(finding?) information to be mapped in parseAnalysis()
     */
    private JSONObject parseRulestoJSON(Path toolOutput) {
        // Always this filename?
        final String rulesOutput = "static-analysis-output-all_rules_metrics_turnedon.txt";

        JSONObject jsonResults = new JSONObject();
        //example: [ERROR]         Final Exam: CookieProcess [Device: PLC Logic: Final_Proj](Line 3 (Decl)): SA0033:  Unused Variable 'StartPB'
        int lineNumber = 0;//PARSE ME FROM FILE

        return jsonResults;
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
