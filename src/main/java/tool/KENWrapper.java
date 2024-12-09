package tool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import utilities.HelperFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class KENWrapper extends Tool implements ITool {
    private static final Logger LOGGER = LoggerFactory.getLogger(CODESYSWrapper.class);

    public KENWrapper() {
        super("KEN", null);
    }

    @Override
    public Path analyze(Path path) {
        //ignore for now, need Ken tool PR approved, and need ability to redirect output
        return path;
    }

    @Override
    public Map<String, Diagnostic> parseAnalysis(Path toolResults) {
        System.out.println("Parsing analysis from " + this.getName() + " on project: " + toolResults);
        LOGGER.debug("Parsing analysis from " + this.getName() + " on project: " + toolResults);

        Map<String, Diagnostic> diagnostics = HelperFunctions.initializeDiagnostics(this.getName());

        if (toolResults.toFile().isDirectory()) {
            for (File benchmarkOutputFile : requireNonNull(toolResults.toFile().listFiles())) {
                if (FilenameUtils.getExtension(benchmarkOutputFile.getName()).equals("json")){
                    //found KEN output file, begin parsing
                    try{
                        String stringResults = HelperFunctions.readFileContent(benchmarkOutputFile.toPath());
                        JSONObject jsonResults = new JSONObject(stringResults);
                        String numberOfLines = (jsonResults.optJSONObject("Number_of_lines")).get("value").toString();
                        String codeSize = (jsonResults.optJSONObject("File_size")).get("value").toString();
                        String numberRungs = (jsonResults.optJSONObject("Number_of_rungs")).get("value").toString();
                        String numberPOUs = (jsonResults.optJSONObject("Number_of_POUs")).get("value").toString();
                        String programNames = (jsonResults.optJSONObject("Program_names")).get("value").toString();
                        String number61131LanguagesUsed = (jsonResults.optJSONObject("Number_of_languages")).get("value").toString();

                    }catch (JSONException e){
                        e.printStackTrace();
                        LOGGER.error("JSON parsing error in parseAnalysis of KENWrapper: \n\t" + e);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOGGER.error("File import parsing error in parseAnalysis of KENWrapper: \n\t" + e);
                    }
                }
            }
        }

        return diagnostics;
    }

    @Override
    public Path initialize(Path path) {
        return null;
    }
}
