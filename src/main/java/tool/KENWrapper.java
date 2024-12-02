package tool;

import pique.analysis.ITool;
import pique.analysis.Tool;
import pique.model.Diagnostic;

import java.nio.file.Path;
import java.util.Map;

public class KENWrapper extends Tool implements ITool {


    public KENWrapper(String name, Path toolRoot) {
        super("KEN", toolRoot);
    }

    @Override
    public Path analyze(Path path) {
        return null;
    }

    @Override
    public Map<String, Diagnostic> parseAnalysis(Path path) {
        return null;
    }

    @Override
    public Path initialize(Path path) {
        return null;
    }
}
