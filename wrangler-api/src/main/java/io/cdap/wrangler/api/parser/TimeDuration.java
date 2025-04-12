/*
 * Copyright © 2025.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class for parsing time durations. (e.g. 10s, 1ms, 2.5ms)
 */
public class TimeDuration implements Token {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)(MS|S)", Pattern.CASE_INSENSITIVE);
    private Double value;
    private String unit;

    /**
     * Constructs {@code TimeDuration} object from the given text
     *
     * @param text String representing the time duration (e.g. 10s, 1ms, 2.5ms)
     * @throws IllegalArgumentException if the given text is not a valid time duration
     */
    public TimeDuration(String text) {
        Matcher matcher = TIME_PATTERN.matcher(text.trim());
        if (matcher.matches()) {
            this.value = Double.parseDouble(matcher.group(1));
            this.unit = matcher.group(3).toUpperCase();
        } else {
            throw new IllegalArgumentException("Invalid time duration: " + text);
        }
    }

    /**
     * Returns the value of the time duration
     *
     * @return value
     */
    public Double getValue() {
        return value;
    }

    /**
     * Returns the unit of the time duration
     *
     * @return unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the time duration in nanoseconds
     * @return nanos
     * @throws IllegalArgumentException if the unit is not "MS" or "S"
     */
    public Double getNanos() {
        switch (unit) {
            case "MS":
                return value * 1000000.0;
            case "S":
                return value * 1000000.0 * 1000.0;
            default:
                throw new IllegalArgumentException("Invalid unit: " + unit);
        }
    }

    public static Double convertNanosToUnit(Double nanos, String unit) {
        if (nanos == 0) {
            return 0.0;
        }
        switch (unit.toUpperCase()) {
            case "NS":
                return nanos;
            case "MS":
                return nanos / 1000000.0;
            case "S":
                return nanos / 1000000.0 / 1000.0;
            default:
                throw new IllegalArgumentException("Invalid unit: " + unit);
        }
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.TIME_DURATION.name());
        object.addProperty("value", value);
        object.addProperty("unit", unit);
        return object;
    }
}
