package dev.chaws.automaticinventory.common;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("LocationRange")
public class LocationRange implements ConfigurationSerializable {
	private final String name;
	private final Location min;
	private final Location max;

	public LocationRange(String name, Location min, Location max) {
		this.name = name;
		this.min = min;
		this.max = max;
	}

	public String getName() {
		return this.name;
	}

	public Location getMin() {
		return this.min;
	}

	public Location getMax() {
		return this.max;
	}

	@NotNull
	@Override
	public Map<String, Object> serialize() {
		var result = new LinkedHashMap<String, Object>();
		result.put("name", this.name);
		result.put("min", this.min);
		result.put("max", this.max);

		return result;
	}

	@NotNull
	public static LocationRange deserialize(Map<String, Object> args) {
		var name = (String)args.get("name");
		var min = (Location)args.get("min");
		var max = (Location)args.get("min");

		return new LocationRange(name, min, max);
	}
}
