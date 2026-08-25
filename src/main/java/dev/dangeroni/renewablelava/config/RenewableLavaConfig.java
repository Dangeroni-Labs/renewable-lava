package dev.dangeroni.renewablelava.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.dangeroni.renewablelava.RenewableLava;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public final class RenewableLavaConfig {
	public static final boolean DEFAULT_ENABLED = true;
	public static final int DEFAULT_REQUIRED_SOURCE_NEIGHBOURS = 2;
	public static final List<String> DEFAULT_WHITELIST_DIMENSIONS = List.of(
		"minecraft:overworld",
		"minecraft:the_nether",
		"minecraft:the_end"
	);

	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private static final String CONFIG_FILE_NAME = "renewable-lava.json";

	private static RenewableLavaConfig current = defaults();

	private final boolean enabled;
	private final int requiredSourceNeighbours;
	private final Set<Identifier> whitelistDimensions;

	private RenewableLavaConfig(boolean enabled, int requiredSourceNeighbours, Set<Identifier> whitelistDimensions) {
		this.enabled = enabled;
		this.requiredSourceNeighbours = requiredSourceNeighbours;
		this.whitelistDimensions = Set.copyOf(whitelistDimensions);
	}

	public static synchronized void load() {
		current = load(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME));
	}

	static synchronized RenewableLavaConfig load(Path path) {
		if (Files.notExists(path)) {
			RenewableLavaConfig defaults = defaults();
			writeDefaults(path, defaults);
			return defaults;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement root = JsonParser.parseReader(reader);
			if (!root.isJsonObject()) {
				RenewableLava.LOGGER.warn("Invalid config format in {}, using defaults.", path.getFileName());
				return defaults();
			}

			return fromJson(root.getAsJsonObject());
		} catch (IOException | JsonParseException exception) {
			RenewableLava.LOGGER.warn("Failed to load {}, using defaults.", path.getFileName());
			return defaults();
		}
	}

	public static synchronized RenewableLavaConfig get() {
		return current;
	}

	static RenewableLavaConfig fromJson(JsonObject root) {
		boolean enabled = readEnabled(root);
		int requiredSourceNeighbours = readRequiredSourceNeighbours(root);
		Set<Identifier> whitelistDimensions = readWhitelistDimensions(root);
		return new RenewableLavaConfig(enabled, requiredSourceNeighbours, whitelistDimensions);
	}

	public static RenewableLavaConfig defaults() {
		return new RenewableLavaConfig(
			DEFAULT_ENABLED,
			DEFAULT_REQUIRED_SOURCE_NEIGHBOURS,
			sanitizeWhitelistDimensions(DEFAULT_WHITELIST_DIMENSIONS)
		);
	}

	public boolean enabled() {
		return this.enabled;
	}

	public int requiredSourceNeighbours() {
		return this.requiredSourceNeighbours;
	}

	public boolean isDimensionWhitelisted(Identifier dimensionId) {
		return this.whitelistDimensions.contains(dimensionId);
	}

	Set<Identifier> whitelistDimensions() {
		return this.whitelistDimensions;
	}

	static int sanitizeRequiredSourceNeighbours(int value) {
		return Math.clamp(value, 2, 4);
	}

	static Set<Identifier> sanitizeWhitelistDimensions(List<String> dimensionIds) {
		LinkedHashSet<Identifier> whitelist = new LinkedHashSet<>();
		for (String dimensionId : dimensionIds) {
			Identifier parsed = Identifier.tryParse(dimensionId);
			if (parsed == null) {
				RenewableLava.LOGGER.warn("Ignoring malformed dimension id '{}' in renewable lava config.", dimensionId);
				continue;
			}

			whitelist.add(parsed);
		}

		return whitelist;
	}

	private static boolean readEnabled(JsonObject root) {
		JsonElement element = root.get("enabled");
		if (element == null) {
			return DEFAULT_ENABLED;
		}

		if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
			RenewableLava.LOGGER.warn("Invalid 'enabled' value in renewable lava config, using default.");
			return DEFAULT_ENABLED;
		}

		return element.getAsBoolean();
	}

	private static int readRequiredSourceNeighbours(JsonObject root) {
		JsonElement element = root.get("requiredSourceNeighbours");
		if (element == null) {
			return DEFAULT_REQUIRED_SOURCE_NEIGHBOURS;
		}

		if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
			RenewableLava.LOGGER.warn("Invalid 'requiredSourceNeighbours' value in renewable lava config, using default.");
			return DEFAULT_REQUIRED_SOURCE_NEIGHBOURS;
		}

		return sanitizeRequiredSourceNeighbours(element.getAsInt());
	}

	private static Set<Identifier> readWhitelistDimensions(JsonObject root) {
		JsonElement element = root.get("whitelistDimensions");
		if (element == null) {
			return sanitizeWhitelistDimensions(DEFAULT_WHITELIST_DIMENSIONS);
		}

		if (!element.isJsonArray()) {
			RenewableLava.LOGGER.warn("Invalid 'whitelistDimensions' value in renewable lava config, using defaults.");
			return sanitizeWhitelistDimensions(DEFAULT_WHITELIST_DIMENSIONS);
		}

		JsonArray array = element.getAsJsonArray();
		LinkedHashSet<String> dimensionIds = new LinkedHashSet<>();
		for (JsonElement entry : array) {
			if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
				RenewableLava.LOGGER.warn("Ignoring non-string whitelist entry in renewable lava config.");
				continue;
			}

			dimensionIds.add(entry.getAsString());
		}

		return sanitizeWhitelistDimensions(List.copyOf(dimensionIds));
	}

	private static void writeDefaults(Path path, RenewableLavaConfig config) {
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config.toJson(), writer);
			}
		} catch (IOException exception) {
			RenewableLava.LOGGER.warn("Failed to create default renewable lava config at {}.", path);
		}
	}

	private JsonObject toJson() {
		JsonObject root = new JsonObject();
		root.addProperty("enabled", this.enabled);
		root.addProperty("requiredSourceNeighbours", this.requiredSourceNeighbours);

		JsonArray whitelist = new JsonArray();
		for (Identifier dimensionId : this.whitelistDimensions) {
			whitelist.add(dimensionId.toString());
		}

		root.add("whitelistDimensions", whitelist);
		return root;
	}
}
