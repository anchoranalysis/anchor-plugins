package org.anchoranalysis.plugin.mpp.experiment.bean.feature.source;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.io.csv.RowLabels;
import org.anchoranalysis.feature.io.name.CombinedName;
import org.anchoranalysis.feature.io.name.MultiName;
import org.anchoranalysis.feature.io.name.SimpleName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class IdentifierHelper {

    public static RowLabels identifierFor(
            String imageIdentifier,
            String objectIdentifier,
            Optional<String> groupGeneratorName,
            String providerName,
            boolean multipleProviders) {
        return new RowLabels(
                Optional.of(new String[] {imageIdentifier, objectIdentifier}),
                createGroupName(groupGeneratorName, providerName, multipleProviders));
    }

    private static Optional<MultiName> createGroupName(
            Optional<String> groupGeneratorName, String providerName, boolean multipleProviders) {
        if (multipleProviders) {
            if (groupGeneratorName.isPresent()) {
                return Optional.of(new CombinedName(groupGeneratorName.get(), providerName));
            } else {
                return Optional.of(new SimpleName(providerName));
            }
        } else {
            return groupGeneratorName.map(SimpleName::new);
        }
    }
}
