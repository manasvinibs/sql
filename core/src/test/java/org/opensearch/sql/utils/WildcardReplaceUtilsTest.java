/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WildcardReplaceUtilsTest {

  @Test
  void testConvertToRegexPatternNoWildcard() {
    assertEquals("TEST", WildcardReplaceUtils.convertToRegexPattern("TEST"));
    assertEquals("CLERK", WildcardReplaceUtils.convertToRegexPattern("CLERK"));
    assertEquals("", WildcardReplaceUtils.convertToRegexPattern(""));
  }

  @Test
  void testConvertToRegexPatternWithWildcardEnd() {
    assertEquals("^\\QCLERK\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("CLERK*"));
    assertEquals("^\\QTEST\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("TEST*"));
  }

  @Test
  void testConvertToRegexPatternWithWildcardStart() {
    assertEquals("(.*)\\QCLERK\\E$", WildcardReplaceUtils.convertToRegexPattern("*CLERK"));
    assertEquals("(.*)\\QMAN\\E$", WildcardReplaceUtils.convertToRegexPattern("*MAN"));
  }

  @Test
  void testConvertToRegexPatternWithWildcardBothEnds() {
    assertEquals("(.*)\\QCLERK\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("*CLERK*"));
    assertEquals("(.*)\\QMAN\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("*MAN*"));
  }

  @Test
  void testConvertToRegexReplacementNoWildcard() {
    assertEquals("EMPLOYEE", WildcardReplaceUtils.convertToRegexReplacement("EMPLOYEE"));
    assertEquals("PERSON", WildcardReplaceUtils.convertToRegexReplacement("PERSON"));
    assertEquals("", WildcardReplaceUtils.convertToRegexReplacement(""));
  }

  @Test
  void testConvertToRegexReplacementWithWildcardEnd() {
    assertEquals("EMPLOYEE$1", WildcardReplaceUtils.convertToRegexReplacement("EMPLOYEE*"));
    assertEquals("PERSON$1", WildcardReplaceUtils.convertToRegexReplacement("PERSON*"));
  }

  @Test
  void testConvertToRegexReplacementWithWildcardStart() {
    assertEquals("$1EMPLOYEE", WildcardReplaceUtils.convertToRegexReplacement("*EMPLOYEE"));
    assertEquals("$1PERSON", WildcardReplaceUtils.convertToRegexReplacement("*PERSON"));
  }

  @Test
  void testConvertToRegexReplacementWithWildcardBothEnds() {
    assertEquals("$1EMPLOYEE$2", WildcardReplaceUtils.convertToRegexReplacement("*EMPLOYEE*"));
    assertEquals("$1PERSON$2", WildcardReplaceUtils.convertToRegexReplacement("*PERSON*"));
  }

  @Test
  void testValidPatternsNoWildcard() {
    // Should not throw any exceptions
    WildcardReplaceUtils.validatePatterns("TEST", "REPLACE");
    WildcardReplaceUtils.validatePatterns("CLERK", "EMPLOYEE");
  }

  @Test
  void testValidPatternsWithWildcards() {
    // Valid combinations
    WildcardReplaceUtils.validatePatterns("CLERK*", "EMPLOYEE*");
    WildcardReplaceUtils.validatePatterns("*MAN", "*PERSON");
    WildcardReplaceUtils.validatePatterns("*TEST*", "NEW*");
    WildcardReplaceUtils.validatePatterns("TEST*", "REPLACE");
  }

  @Test
  void testInvalidConsecutiveWildcards() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> WildcardReplaceUtils.validatePatterns("CLERK**", "EMPLOYEE*"));
    assertEquals("Consecutive wildcards are not supported", ex.getMessage());

    ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> WildcardReplaceUtils.validatePatterns("CLERK*", "EMPLOYEE**"));
    assertEquals("Consecutive wildcards are not supported", ex.getMessage());
  }

  @Test
  void testInvalidWildcardInReplacementOnly() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> WildcardReplaceUtils.validatePatterns("CLERK", "EMPLOYEE*"));
    assertEquals(
        "If replacement contains wildcard, pattern must contain wildcard", ex.getMessage());
  }

  @Test
  void testInvalidWildcardCount() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> WildcardReplaceUtils.validatePatterns("TEST*", "NEW*TEXT*"));
    assertEquals(
        "Number of wildcards in replacement cannot exceed number of wildcards in pattern",
        ex.getMessage());
  }

  @Test
  void testValidComplexPatterns() {
    // Pattern has more wildcards than replacement
    WildcardReplaceUtils.validatePatterns("*TEST*END*", "*NEW*");
    WildcardReplaceUtils.validatePatterns("*PRE*MID*", "START*END");
    WildcardReplaceUtils.validatePatterns("*START*END*", "*REPLACE");
  }

  @Test
  void testSpecialCharactersInPatterns() {
    assertEquals("^\\Q$TEST\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("$TEST*"));
    assertEquals("(.*)\\Q[TEST]\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("*[TEST]*"));
    assertEquals("(.*)\\Q.TEST.\\E$", WildcardReplaceUtils.convertToRegexPattern("*.TEST."));
  }

  @Test
  void testEmptyPatternWithWildcard() {
    // Test single wildcard pattern first
    assertEquals("(.*)", WildcardReplaceUtils.convertToRegexPattern("*"));

    // Test wildcard at start
    assertEquals("(.*)\\Qa\\E$", WildcardReplaceUtils.convertToRegexPattern("*a"));

    // Test wildcard at end
    assertEquals("^\\Qa\\E(.*)", WildcardReplaceUtils.convertToRegexPattern("a*"));

    // Test consecutive wildcards - should throw exception
    assertThrows(
        IllegalArgumentException.class, () -> WildcardReplaceUtils.validatePatterns("**", "TEST"));
  }

  @Test
  void testEdgeCasePatterns() {
    // Single character patterns
    WildcardReplaceUtils.validatePatterns("a*", "b*");
    WildcardReplaceUtils.validatePatterns("*a", "*b");
    WildcardReplaceUtils.validatePatterns("*a*", "*b*");

    // Empty patterns
    WildcardReplaceUtils.validatePatterns("", "");
    WildcardReplaceUtils.validatePatterns("*", "text");
  }
}
