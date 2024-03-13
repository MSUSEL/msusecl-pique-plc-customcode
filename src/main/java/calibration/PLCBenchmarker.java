package calibration;

import pique.calibration.NaiveBenchmarker;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PLCBenchmarker extends NaiveBenchmarker {

    @Override
    public Set<Path> CollectProjectPaths(Path benchmarkRepository, String projectRootFlag) {
        Set<Path> projectRoots = new HashSet<>();

        File[] subDirectories = benchmarkRepository.toFile().listFiles();
        for (File file : subDirectories) {
            projectRoots.add(file.toPath());
        }
        return projectRoots;
    }
}
