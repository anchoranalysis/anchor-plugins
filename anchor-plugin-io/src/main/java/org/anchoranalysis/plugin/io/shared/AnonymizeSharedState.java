package org.anchoranalysis.plugin.io.shared;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class AnonymizeSharedState {
    
    private NumberToStringConverter numberConverter;
    
    // Create a mapping between each index
    private Map<Integer,Integer> mapping;
}
