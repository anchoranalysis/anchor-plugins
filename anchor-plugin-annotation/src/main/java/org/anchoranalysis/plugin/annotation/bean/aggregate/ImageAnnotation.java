/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.aggregate;

class ImageAnnotation {

    private String identifier;
    private String label;

    public ImageAnnotation(String identifier, String label) {
        super();
        this.identifier = identifier;
        this.label = label;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLabel() {
        return label;
    }
}
