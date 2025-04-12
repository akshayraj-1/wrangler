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
 * Class for parsing byte sizes. (e.g. 10KB, 1MB, 2.5MB, 3.5GB)
 */
public class ByteSize implements Token {

    private static final Pattern BYTE_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)([KMG]B)", Pattern.CASE_INSENSITIVE);
    private Double value;
    private String unit;

    /**
     * Constructs {@code ByteSize} object from the given text
     *
     * @param text String representation of the byte size (e.g. 10KB, 1MB, 2.5MB, 3.5GB)
     * @throws IllegalArgumentException if the given text is not a valid byte size
     */
    public ByteSize(String text) {
        Matcher matcher = BYTE_PATTERN.matcher(text.trim());
        if (matcher.matches()) {
            this.value = Double.parseDouble(matcher.group(1));
            this.unit = matcher.group(3).toUpperCase();
        } else {
            throw new IllegalArgumentException("Invalid byte size: " + text);
        }
    }

    /**
     * Returns the value of the byte size
     *
     * @return value
     */
    public Double getValue() {
        return value;
    }

    /**
     * Returns the unit of the byte size
     *
     * @return unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the byte size in bytes
     * @return bytes
     * @throws IllegalArgumentException if the unit is not "B", "K", "M", or "G"
     */
    public Long getBytes() {
        switch (unit) {
            case "B":
                return value.longValue();
            case "KB":
                return (long) (value * 1024.0);
            case "MB":
                return (long) (value * 1024.0 * 1024.0);
            case "GB":
                return (long) (value * 1024.0 * 1024.0 * 1024.0);
            default:
                throw new IllegalArgumentException("Invalid unit: " + unit);
        }
    }

    public static Double convertByteToUnit(Double value, String unit) {
        switch (unit.toUpperCase()) {
            case "B":
                return value;
            case "KB":
                return value / 1024.0;
            case "MB":
                return value / 1024.0 / 1024.0;
            case "GB":
                return value / 1024.0 / 1024.0 / 1024.0;
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
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.BYTE_SIZE.name());
        object.addProperty("value", value);
        object.addProperty("unit", unit);
        return object;
    }
}
