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

package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link TimeDuration} token type
 */
public class TimeDurationTest {

    private final String[] timeDurations = { "1.5ms", "1s", "2.1s" };
    private final String[] invalidTimeDurations = { "1.5", "1", "2.1" };
    private final Double[] expected = { 1500000.0, 1000000000.0, 2100000000.0 };

    @Test
    public void testValidTimeDuration() {
        for (int i = 0; i < timeDurations.length; i++) {
            TimeDuration timeDuration = new TimeDuration(timeDurations[i]);
            Assert.assertEquals(expected[i], timeDuration.getNanos());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        for (String invalidTimeDuration : invalidTimeDurations) {
            new TimeDuration(invalidTimeDuration);
        }
    }

}
