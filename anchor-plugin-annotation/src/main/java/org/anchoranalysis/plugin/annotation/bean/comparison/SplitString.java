/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison;

class SplitString {

    private String[] splitParts;

    public SplitString(String input, String regexSplit) {
        splitParts = input.split(regexSplit);
    }

    public int getNumParts() {
        return splitParts.length;
    }

    public String getSplitPartOrEmptyString(int index) {
        if (index < splitParts.length) {
            return splitParts[index];
        } else {
            return "";
        }
    }

    public String combineFirstElements(int maxNumElements, String combineSeperator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxNumElements; i++) {

            // In case we don't have enough elements
            if (i >= splitParts.length) {
                break;
            }

            if (i != 0) {
                sb.append(combineSeperator);
            }

            sb.append(splitParts[i]);
        }
        return sb.toString();
    }
}
