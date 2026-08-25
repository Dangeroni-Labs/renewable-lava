package dev.dangeroni.renewablelava.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenewableLavaConfigTest {
	@Test
	void clampsRequiredSourceNeighboursIntoSupportedRange() {
		assertEquals(2, RenewableLavaConfig.sanitizeRequiredSourceNeighbours(0));
		assertEquals(2, RenewableLavaConfig.sanitizeRequiredSourceNeighbours(2));
		assertEquals(4, RenewableLavaConfig.sanitizeRequiredSourceNeighbours(7));
	}

	@Test
	void acceptsValidDimensionIdentifiersAndSkipsMalformedOnes() {
		Set<Identifier> whitelist = RenewableLavaConfig.sanitizeWhitelistDimensions(
			java.util.List.of("minecraft:overworld", "bad id", "example:custom"),
			false
		);

		assertTrue(whitelist.contains(Identifier.withDefaultNamespace("overworld")));
		assertTrue(whitelist.contains(Identifier.fromNamespaceAndPath("example", "custom")));
		assertFalse(whitelist.stream().anyMatch(id -> id.toString().equals("bad id")));
	}

	@Test
	void fallsBackToSafeDefaultsForInvalidFields() {
		JsonObject root = new JsonObject();
		root.addProperty("enabled", true);
		root.addProperty("requiredSourceNeighbours", 9);
		JsonArray whitelist = new JsonArray();
		whitelist.add("minecraft:the_nether");
		whitelist.add("not valid");
		root.add("whitelistDimensions", whitelist);

		RenewableLavaConfig config = RenewableLavaConfig.fromJson(root, false);

		assertTrue(config.enabled());
		assertEquals(4, config.requiredSourceNeighbours());
		assertTrue(config.isDimensionWhitelisted(Identifier.withDefaultNamespace("the_nether")));
		assertFalse(config.isDimensionWhitelisted(Identifier.withDefaultNamespace("overworld")));
	}
}
