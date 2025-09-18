package org.opensearch.sql.utils;

import java.util.regex.Pattern;

/** Utility class for handling wildcard patterns in replace operations. */
public class WildcardReplaceUtils {

  /**
   * Convert wildcard pattern to regex pattern for REGEXP_REPLACE.
   *
   * @param pattern Pattern that may contain wildcards
   * @return Regex pattern
   */
  public static String convertToRegexPattern(String pattern) {
    if (pattern == null || pattern.isEmpty()) {
      return pattern;
    }

    // If not a wildcard pattern, return as is
    if (!WildcardRenameUtils.isWildcardPattern(pattern)) {
      return pattern;
    }

    // Check for consecutive wildcards before any substring operations
    if (pattern.matches(".*\\*{2,}.*")) {
      throw new IllegalArgumentException("Consecutive wildcards are not supported");
    }

    // Handle single wildcard pattern
    if (pattern.equals("*")) {
      return "(.*)";
    }

    // Handle different wildcard positions
    if (pattern.startsWith("*") && pattern.endsWith("*")) {
      // *abc* -> Pattern matches 'abc' anywhere
      String middle = pattern.substring(1, pattern.length() - 1);
      return "(.*)" + Pattern.quote(middle) + "(.*)";
    } else if (pattern.startsWith("*")) {
      // *abc -> Pattern matches 'abc' at end
      String end = pattern.substring(1);
      return "(.*)" + Pattern.quote(end) + "$";
    } else if (pattern.endsWith("*")) {
      // abc* -> Pattern matches 'abc' at start with explicit capture group
      String start = pattern.substring(0, pattern.length() - 1);
      return "^" + Pattern.quote(start) + "(.*)"; // Explicitly create capture group
    }
    return pattern;
  }

  /**
   * Convert wildcard replacement to regex replacement. Converts * to corresponding regex group
   * references ($1, $2, etc.)
   *
   * @param replacement Replacement pattern with wildcards
   * @return Regex replacement string
   */
  public static String convertToRegexReplacement(String replacement) {
    if (!WildcardRenameUtils.isWildcardPattern(replacement)) {
      return replacement;
    }
    if (replacement.startsWith("*") && replacement.endsWith("*")) {
      // *XYZ* -> Replacement with both prefix and suffix captured content
      String middle = replacement.substring(1, replacement.length() - 1);
      return "$1" + middle + "$2";
    } else if (replacement.startsWith("*")) {
      // *XYZ -> Replacement with prefix captured content
      String end = replacement.substring(1);
      return "$1" + end;
    } else if (replacement.endsWith("*")) {
      // XYZ* -> Replacement with suffix captured content
      String start = replacement.substring(0, replacement.length() - 1);
      return start + "$1";
    }
    return replacement;
  }

  /**
   * Validate wildcard patterns compatibility.
   *
   * @param pattern Source pattern
   * @param replacement Replacement pattern
   * @throws IllegalArgumentException if patterns are invalid
   */
  public static void validatePatterns(String pattern, String replacement) {
    if (WildcardRenameUtils.isWildcardPattern(pattern)
        || WildcardRenameUtils.isWildcardPattern(replacement)) {
      if (pattern.matches(".*\\*{2,}.*") || replacement.matches(".*\\*{2,}.*")) {
        throw new IllegalArgumentException("Consecutive wildcards are not supported");
      }
    }

    // If replacement has wildcard, pattern must have wildcard
    if (WildcardRenameUtils.isWildcardPattern(replacement)
        && !WildcardRenameUtils.isWildcardPattern(pattern)) {
      throw new IllegalArgumentException(
          "If replacement contains wildcard, pattern must contain wildcard");
    }

    // Check if wildcard count matches
    if (WildcardRenameUtils.isWildcardPattern(replacement)) {
      long patternWildcards = pattern.chars().filter(ch -> ch == '*').count();
      long replacementWildcards = replacement.chars().filter(ch -> ch == '*').count();

      if (replacementWildcards > patternWildcards) {
        throw new IllegalArgumentException(
            "Number of wildcards in replacement cannot exceed number of wildcards in pattern");
      }
    }
  }
}
