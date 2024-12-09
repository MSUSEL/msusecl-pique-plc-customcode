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
package model;

import com.google.gson.JsonObject;
import pique.evaluation.*;
import pique.model.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PLCQualityModelImport extends QualityModelImport {

    public PLCQualityModelImport(Path qmFileLocation) {
        super(qmFileLocation);
    }

    protected Map<String, ModelNode> instanceDiagnosticsFromJson(JsonObject diagnosticsJson) {

        Map<String, ModelNode> tempDiagnostics = new HashMap<>();

        diagnosticsJson.entrySet().forEach(entry -> {

            JsonObject jsonDiagnostic = entry.getValue().getAsJsonObject();
            String diagnosticName = entry.getKey();
            String diagnosticDescription = jsonDiagnostic.get("description").getAsString();
            String diagnosticToolName = jsonDiagnostic.get("toolName").getAsString();
            String importance = "";
            if (jsonDiagnostic.get("importance") != null) {
                importance = jsonDiagnostic.get("importance").getAsString();
            }
            IEvaluator evaluator = getEvaluatorFromConfiguration(jsonDiagnostic, "diagnostic");
            INormalizer normalizer = getNormalizerFromConfiguration(jsonDiagnostic);
            IUtilityFunction utilityFunction = getUtilityFunctionFromConfiguration(jsonDiagnostic);
            Map<String, BigDecimal> weights = getWeightsFromConfiguration(jsonDiagnostic);
            BigDecimal[] thresholds = getThresholdsFromConfiguration(jsonDiagnostic);

            // Instance the diagnostic

            Diagnostic d;
            if (importance.equals("")) {
                d = new Diagnostic(diagnosticName, diagnosticDescription, diagnosticToolName,
                        evaluator, normalizer, utilityFunction, weights, thresholds);
            } else {
                d = new CODESYSRuleDiagnostic(diagnosticName, diagnosticDescription, diagnosticToolName,
                        evaluator, normalizer, utilityFunction, weights, thresholds, importance);
            }

            // Add to the collection
            if (tempDiagnostics.containsKey(d.getName())) {
                throw new RuntimeException("Two diagnostics with the same name were found while parsing the " +
                        "quality model file.");
            }
            tempDiagnostics.put(d.getName(), d);
        });

        return tempDiagnostics;
    }
}