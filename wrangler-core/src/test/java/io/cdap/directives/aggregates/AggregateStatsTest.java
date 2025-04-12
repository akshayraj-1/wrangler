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

package io.cdap.directives.aggregates;

import io.cdap.cdap.etl.api.Lookup;
import io.cdap.cdap.etl.api.StageMetrics;
import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.proto.Contexts;
import org.junit.Assert;
import org.junit.Test;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Tests for {@link AggregateStats}
 */
public class AggregateStatsTest {

    @Test
    public void testAggregateStatsTotal() throws Exception {
        String[] recipe = new String[] {
                "aggregate-stats :data_size :response_time :total_size :total_time total",
        };

        List<Row> rows = new ArrayList<>();
        rows.add(new Row("data_size", new ByteSize("10kb"))
                .add("response_time", new TimeDuration("1.5ms"))
        );
        rows.add(new Row("data_size", new ByteSize("20Kb"))
                .add("response_time", new TimeDuration("2.5Ms"))
        );
        rows.add(new Row("data_size", new ByteSize("35.6MB"))
                .add("response_time", new TimeDuration("35s"))
        );

        List<Row> result = getAggregatedRows(recipe, rows);
        Assert.assertEquals(1, result.size());

        double expectedTotalBytes = (10 * 1024 + 20 * 1024 + 35.6 * 1024 * 1024);
        double expectedTotalNanos = 1_500_000 + 2_500_000 + 35_000_000_000L;

        double gotTotalBytes = (double) result.get(0).getFields().get(0).getSecond();
        double gotTotalNanos = (double) result.get(0).getFields().get(1).getSecond();

        Assert.assertEquals(expectedTotalBytes, gotTotalBytes, 0.01);
        Assert.assertEquals(expectedTotalNanos, gotTotalNanos, 0.01);

    }

    @Test
    public void testAggregateStatsAvg() throws Exception {
        String[] recipe = new String[] {
                "aggregate-stats :data_size :response_time :avg_size :avg_time 'average'",
        };

        List<Row> rows = new ArrayList<>();
        rows.add(new Row("data_size", new ByteSize("10kb"))
                .add("response_time", new TimeDuration("1.5ms"))
        );
        rows.add(new Row("data_size", new ByteSize("20Kb"))
                .add("response_time", new TimeDuration("2.5Ms"))
        );
        rows.add(new Row("data_size", new ByteSize("35.6MB"))
                .add("response_time", new TimeDuration("35s"))
        );

        List<Row> result = getAggregatedRows(recipe, rows);
        Assert.assertEquals(1, result.size());

        double expectedAvgBytes = (10 * 1024 + 20 * 1024 + 35.6 * 1024 * 1024) / 3.0;
        double expectedAvgNanos = (1.5 * 1_000_000 + 2.5 * 1_000_000 + 35 * 1_000_000_000L) / 3.0;

        double gotAvgBytes = (double) result.get(0).getFields().get(0).getSecond();
        double gotAvgNanos = (double) result.get(0).getFields().get(1).getSecond();

        Assert.assertEquals(expectedAvgBytes, gotAvgBytes, 0.01);
        Assert.assertEquals(expectedAvgNanos, gotAvgNanos, 0.01);

    }

    @Test
    public void testAggregateStatsDifferentUnits() throws Exception {
        String[] recipe = new String[] {
                "aggregate-stats :data_size :response_time :total_size_mb :total_time_secs 'total' 'MB' 'S'",
        };

        List<Row> rows = new ArrayList<>();
        rows.add(new Row("data_size", new ByteSize("10kb"))
                .add("response_time", new TimeDuration("1.5ms"))
        );
        rows.add(new Row("data_size", new ByteSize("20Kb"))
                .add("response_time", new TimeDuration("2.5Ms"))
        );
        rows.add(new Row("data_size", new ByteSize("35.6MB"))
                .add("response_time", new TimeDuration("35s"))
        );

        List<Row> result = getAggregatedRows(recipe, rows);
        Assert.assertEquals(1, result.size());

        double expectedTotalMB = (10 * 1024 + 20 * 1024 + 35.6 * 1024 * 1024) / (1024.0 * 1024.0);
        double expectedTotalSecs = (1.5 * 1_000_000 + 2.5 * 1_000_000 + 35 * 1_000_000_000L) / 1_000_000_000.0;

        double gotTotalMB = (double) result.get(0).getFields().get(0).getSecond();
        double gotTotalSecs = (double) result.get(0).getFields().get(1).getSecond();

        Assert.assertEquals(expectedTotalMB, gotTotalMB, 0.01);
        Assert.assertEquals(expectedTotalSecs, gotTotalSecs, 0.01);

    }


    private List<Row> getAggregatedRows(String[] recipe, List<Row> rows) throws Exception {
        HashMap<String, Object> map = new HashMap<>();

        return TestingRig.execute(recipe, rows, new ExecutorContext() {
            @Override
            public Environment getEnvironment() {
                return Environment.TESTING;
            }

            @Override
            public String getNamespace() {
                return Contexts.SYSTEM;
            }

            @Override
            public StageMetrics getMetrics() {
                return null;
            }

            @Override
            public String getContextName() {
                return "aggregateStats";
            }

            @Override
            public Map<String, String> getProperties() {
                return new HashMap<>();
            }

            @Override
            public URL getService(String applicationId, String serviceId) {
                return null;
            }

            @Override
            public TransientStore getTransientStore() {
                return new TransientStore() {
                    @Override
                    public void reset(TransientVariableScope scope) {

                    }

                    @Override
                    public <T> T get(String name) {
                        Object defaultValue;
                        switch (name) {
                            case "totalSizeInBytes":
                            case "totalDurationInNanos":
                                defaultValue = 0.0;
                                break;
                            case "sizeCount":
                            case "durationCount":
                                defaultValue = 0;
                                break;
                            case "rowsCount":
                                defaultValue = rows.size();
                                break;
                            default:
                                defaultValue = null;
                        }
                        return (T) map.getOrDefault(name, defaultValue);
                    }

                    @Override
                    public void set(TransientVariableScope scope, String name, Object value) {
                        map.put(name, value);
                    }

                    @Override
                    public void increment(TransientVariableScope scope, String name, long value) {

                    }

                    @Override
                    public Set<String> getVariables() {
                        return map.keySet();
                    }
                };
            }

            @Override
            public <T> Lookup<T> provide(String s, Map<String, String> map1) {
                return null;
            }
        });
    }

}
