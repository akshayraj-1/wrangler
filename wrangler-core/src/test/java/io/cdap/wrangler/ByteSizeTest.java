/*
 * Copyright © 2017-2019 Cask Data, Inc.
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


import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link  ByteSize} token type
 */
public class ByteSizeTest {

    private final String[] byteSizes = { "10kb", "1MB", "2.5Mb", "3.5gb" };
    private final String[] invalidByteSizes = { "10k", "1", "2.5M", "3.5g" };
    private final Double[] expected = { 10240.0, 1048576.0, 2621440.0, 3758096384.0 };

    @Test
    public void testValidByteSize() {
        for (int i = 0; i < byteSizes.length; i++) {
            ByteSize byteSize = new ByteSize(byteSizes[i]);
            Assert.assertEquals(expected[i], byteSize.getBytes());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        for (String invalidByteSize : invalidByteSizes) {
            new ByteSize(invalidByteSize);
        }
    }

}
