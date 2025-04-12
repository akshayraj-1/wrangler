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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.ReportErrorAndProceed;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.Collections;
import java.util.List;


/**
 * AggregateStats
 */
@Plugin(type = Directive.TYPE)
@Name(AggregateStats.NAME)
@Description("AggregateStats - Aggregates byte size and time duration statistics from a list of rows")
public class AggregateStats implements Directive {

    public static final String NAME = "aggregate-stats";

    private String sizeSourceColumn;
    private String durationSourceColumn;
    private String targetTotalSizeColumn;
    private String targetTotalDurationColumn;
    private String outputSizeUnitType;
    private String outputTimeUnitType;
    private String aggregateType;


    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
        builder.define("sizeSourceColumn", TokenType.COLUMN_NAME);
        builder.define("durationSourceColumn", TokenType.COLUMN_NAME);
        builder.define("targetTotalSizeColumn", TokenType.COLUMN_NAME);
        builder.define("targetTotalDurationColumn", TokenType.COLUMN_NAME);
        builder.define("aggregateType", TokenType.TEXT, Optional.TRUE);
        builder.define("outputSizeUnitType", TokenType.TEXT, Optional.TRUE);
        builder.define("outputTimeUnitType", TokenType.TEXT, Optional.TRUE);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeSourceColumn = ((ColumnName) args.value("sizeSourceColumn")).value();
        this.durationSourceColumn = ((ColumnName) args.value("durationSourceColumn")).value();
        this.targetTotalSizeColumn = ((ColumnName) args.value("targetTotalSizeColumn")).value();
        this.targetTotalDurationColumn = ((ColumnName) args.value("targetTotalDurationColumn")).value();

        if (args.contains("outputSizeUnitType")) {
            this.outputSizeUnitType = ((Text) args.value("outputSizeUnitType")).value();
        } else {
            this.outputSizeUnitType = "B";
        }

        if (args.contains("outputTimeUnitType")) {
            this.outputTimeUnitType = ((Text) args.value("outputTimeUnitType")).value();
        } else {
            this.outputTimeUnitType = "NS";
        }

        if (args.contains("aggregateType")) {
            this.aggregateType = ((Text) args.value("aggregateType")).value();
        } else {
            this.aggregateType = "total";
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context)
            throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {

        TransientStore store = context.getTransientStore();
        double totalSizeInBytes = store.get("totalSizeInBytes");
        double totalDurationInNanos = store.get("totalDurationInNanos");
        int sizeCount = store.get("sizeCount");
        int durationCount = store.get("durationCount");


        for (Row row : rows) {

            // For ByteSize
            if (row.find(sizeSourceColumn) != -1) {
                Object sizeValue = row.getValue(sizeSourceColumn);
                if (sizeValue instanceof ByteSize) {
                    totalSizeInBytes += ((ByteSize) sizeValue).getBytes();
                    sizeCount++;
                } else {
                    throw new DirectiveExecutionException(
                            "Invalid value " + sizeValue + " in column " + sizeSourceColumn
                    );
                }
            }

            // For TimeDuration
            if (row.find(durationSourceColumn) != -1) {
                Object durationValue = row.getValue(durationSourceColumn);
                if (durationValue instanceof TimeDuration) {
                    totalDurationInNanos += ((TimeDuration) durationValue).getNanos();
                    durationCount++;
                } else {
                    throw new DirectiveExecutionException(
                            "Invalid value " + durationValue + " in column " + durationSourceColumn
                    );
                }
            }
        }

        store.set(TransientVariableScope.LOCAL, "totalSizeInBytes", totalSizeInBytes);
        store.set(TransientVariableScope.LOCAL, "totalDurationInNanos", totalDurationInNanos);
        store.set(TransientVariableScope.LOCAL, "sizeCount", sizeCount);
        store.set(TransientVariableScope.LOCAL, "durationCount", durationCount);

        int rowsCount = store.get("rowsCount");
        if (rowsCount == sizeCount && rowsCount == durationCount) {

            double outputSize;
            double outputDuration;

            // Calculating the required aggregate
            if (aggregateType.equals("average")) {
                outputSize = totalSizeInBytes / rowsCount;
                outputDuration = totalDurationInNanos / rowsCount;
            } else if (aggregateType.equals("total")) {
                outputSize = totalSizeInBytes;
                outputDuration = totalDurationInNanos;
            } else {
                throw new DirectiveExecutionException("Invalid aggregateType: " + aggregateType);
            }

            // Converting to output units
            outputSize = ByteSize.convertByteToUnit(outputSize, outputSizeUnitType);
            outputDuration = TimeDuration.convertNanosToUnit(outputDuration, outputTimeUnitType);

            Row outputRow = new Row();
            outputRow.add(targetTotalSizeColumn, outputSize);
            outputRow.add(targetTotalDurationColumn, outputDuration);

            store.reset(TransientVariableScope.GLOBAL);
            store.reset(TransientVariableScope.LOCAL);

            return Collections.singletonList(outputRow);
        }

        return Collections.emptyList();
    }


    @Override
    public void destroy() {
        // no-op
    }
}
