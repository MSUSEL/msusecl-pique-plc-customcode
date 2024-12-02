package tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;
import utilities.HelperFunctions;

import java.nio.file.Path;
import java.util.Map;

public class KENWrapper extends Tool implements ITool {
    private static final Logger LOGGER = LoggerFactory.getLogger(CODESYSWrapper.class);

    public KENWrapper(String name, Path toolRoot) {
        super("KEN", toolRoot);
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


        return null;
    }

    @Override
    public Path initialize(Path path) {
        return null;
    }
}
