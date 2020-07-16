/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import java.util.Optional;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractGroup {

    public static Optional<String> extractStr(
            int groupID, Matcher matcher, String failureGroupDescription) {

        if (groupID > matcher.groupCount()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(groupID));
    }

    public static int maybeExtractInt(int groupID, String failureDescr, Matcher matcher) {
        if (groupID > 0) {
            return extractInt(groupID, matcher, failureDescr);
        } else {
            return 0;
        }
    }

    private static int extractInt(int groupID, Matcher matcher, String failureGroupDescription) {

        if (groupID > matcher.groupCount()) {
            return -1;
        }

        Optional<String> groupText = extractStr(groupID, matcher, failureGroupDescription);
        return groupText.map(Integer::parseInt).orElse(-1);
    }
}
