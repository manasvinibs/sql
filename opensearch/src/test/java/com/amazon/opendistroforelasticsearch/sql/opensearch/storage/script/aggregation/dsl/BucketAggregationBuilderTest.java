/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.opensearch.storage.script.aggregation.dsl;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.opensearch.data.type.OpenSearchDataType.OPENSEARCH_TEXT_KEYWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opensearch.common.xcontent.ToXContent.EMPTY_PARAMS;

import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.opensearch.storage.serialization.ExpressionSerializer;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.opensearch.search.sort.SortOrder;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class BucketAggregationBuilderTest {

  @Mock
  private ExpressionSerializer serializer;

  private BucketAggregationBuilder aggregationBuilder;

  @BeforeEach
  void set_up() {
    aggregationBuilder = new BucketAggregationBuilder(serializer);
  }

  @Test
  void should_build_bucket_with_field() {
    assertEquals(
        "{\n"
            + "  \"terms\" : {\n"
            + "    \"field\" : \"age\",\n"
            + "    \"missing_bucket\" : true,\n"
            + "    \"order\" : \"asc\"\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                asc(named("age", ref("age", INTEGER))))));
  }

  @Test
  void should_build_bucket_with_keyword_field() {
    assertEquals(
        "{\n"
            + "  \"terms\" : {\n"
            + "    \"field\" : \"name.keyword\",\n"
            + "    \"missing_bucket\" : true,\n"
            + "    \"order\" : \"asc\"\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                asc(named("name", ref("name", OPENSEARCH_TEXT_KEYWORD))))));
  }

  @SneakyThrows
  private String buildQuery(List<Pair<NamedExpression, SortOrder>> groupByExpressions) {
    XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
    builder.startObject();
    CompositeValuesSourceBuilder<?> sourceBuilder =
        aggregationBuilder.build(groupByExpressions).get(0);
    sourceBuilder.toXContent(builder, EMPTY_PARAMS);
    builder.endObject();
    return BytesReference.bytes(builder).utf8ToString();
  }

  private Pair<NamedExpression, SortOrder> asc(NamedExpression expression) {
    return Pair.of(expression, SortOrder.ASC);
  }
}