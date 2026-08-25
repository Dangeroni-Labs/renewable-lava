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
}
