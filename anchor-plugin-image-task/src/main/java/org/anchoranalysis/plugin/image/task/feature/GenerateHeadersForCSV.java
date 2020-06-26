package org.anchoranalysis.plugin.image.task.feature;

import org.anchoranalysis.feature.io.csv.MetadataHeaders;


import java.util.Optional;

public class GenerateHeadersForCSV {

	private final String[] results;
	private final Optional<String> additionalGroupHeader;
	
	public GenerateHeadersForCSV(String[] results, Optional<String> additionalGroupHeader) {
		super();
		this.results = results;
		this.additionalGroupHeader = additionalGroupHeader;
	}
	
	public MetadataHeaders createMetadataHeaders(boolean groupGeneratorDefined) {
		return new MetadataHeaders(
			headersForGroup(groupGeneratorDefined),
			results
		);
	}
	
	private String[] headersForGroup(boolean groupGeneratorDefined) {
		if (groupGeneratorDefined) {
			if (additionalGroupHeader.isPresent()) {
				return new String[]{"group", additionalGroupHeader.get()};
			} else {
				return new String[]{"group"};
			}
		} else {
			if (additionalGroupHeader.isPresent()) {
				return new String[]{additionalGroupHeader.get()};
			} else {
				return new String[]{};
			}
		}
	}
}

