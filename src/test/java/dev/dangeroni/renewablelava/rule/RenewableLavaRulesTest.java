package dev.dangeroni.renewablelava.rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenewableLavaRulesTest {
	@Test
	void requiresConfigWorldAndDimensionToAllBeEnabled() {
		assertTrue(RenewableLavaRules.evaluate(true, true, true));
		assertFalse(RenewableLavaRules.evaluate(false, true, true));
		assertFalse(RenewableLavaRules.evaluate(true, false, true));
		assertFalse(RenewableLavaRules.evaluate(true, true, false));
	}

	@Test
	void sourceEligibilityUsesRequiredHorizontalSourceThreshold() {
		assertFalse(RenewableLavaRules.hasRequiredSourceNeighbours(1, 2));
		assertTrue(RenewableLavaRules.hasRequiredSourceNeighbours(2, 2));
		assertFalse(RenewableLavaRules.hasRequiredSourceNeighbours(2, 3));
		assertTrue(RenewableLavaRules.hasRequiredSourceNeighbours(3, 3));
		assertFalse(RenewableLavaRules.hasRequiredSourceNeighbours(2, 4));
		assertTrue(RenewableLavaRules.hasRequiredSourceNeighbours(4, 4));
	}
}
