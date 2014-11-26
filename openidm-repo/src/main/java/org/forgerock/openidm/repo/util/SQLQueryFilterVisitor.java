/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.repo.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.forgerock.json.fluent.JsonPointer;
import org.forgerock.json.resource.QueryFilter;
import org.forgerock.json.resource.QueryFilterVisitor;
import org.forgerock.util.Iterables;
import org.forgerock.util.promise.Function;
import org.forgerock.util.promise.NeverThrowsException;

/**
 * An abstract {@link QueryFilterVisitor} to produce SQL.  Includes patterns for the standard
 *
 * <ul>
 *     <li>AND</li>
 *     <li>OR</li>
 *     <li>NOT</li>
 *     <li>&gt;=</li>
 *     <li>&gt;</li>
 *     <li>=</li>
 *     <li>&lt;</li>
 *     <li>&lt;=</li>
 * </ul>
 * operators, along with the following implementations for {@link QueryFilter}'s
 * <ul>
 *     <li>contains : field LIKE '%value%'</li>
 *     <li>startsWith : field LIKE 'value%'</li>
 *     <li>literal true : 1 = 1</li>
 *     <li>literal false : 1 &lt;&gt; 1</li>
 * </ul>
 * <p>
 * This implementation does not support extended-match.
 * <p>
 * The implementer is responsible for implementing {@link #visitValueAssertion(Object, String, org.forgerock.json.fluent.JsonPointer, Object)}
 * which handles the value assertions - x operand y for the standard operands.  The implementer is also responsible for
 * implementing {@link #visitPresentFilter(Object, org.forgerock.json.fluent.JsonPointer)} as "field present" can vary
 * by database implementation (though typically "field IS NOT NULL" is chosen).
 */
public abstract class SQLQueryFilterVisitor<P> implements QueryFilterVisitor<String, P> {

    /**
     * A templating method that will generate the actual value assertion.
     * <p>
     * Example:
     * <pre><blockquote>
     *     ?_queryFilter=email+eq+"someone@example.com"
     * </blockquote></pre>
     * is an QueryFilter stating the value assertion "email" equals "someone@example.com".  The correct SQL for that
     * may vary depending on database variant and schema definition.  This method will be invoked as
     * <pre><blockquote>
     *     return visitValueAssertion(parameters, "=", JsonPointer(/email), "someone@example.com");
     * </blockquote></pre>
     * A possible implementation for the above example may be
     * <pre><blockquote>
     *     return getDatabaseColumnFor("email") + "=" + ":email";
     * </blockquote></pre>
     * The parameters argument is implementation-dependent as a way to store placeholder mapping throughout the query-filter visiting.
     *
     * @param parameters storage of parameter-substitutions for the value of the assertion
     * @param operand the operand used to compare
     * @param field the object field as a JsonPointer - implementations need to map this to an appropriate database column
     * @param valueAssertion the value in the assertion
     * @return a query expression or clause
     */
    public abstract String visitValueAssertion(P parameters, String operand, JsonPointer field, Object valueAssertion);

    private String visitCompositeFilter(final P parameters, List<QueryFilter> subFilters, String operand) {
        StringBuilder sb = new StringBuilder("(");
        sb.append(StringUtils.join(
                Iterables.from(subFilters)
                        .map(new Function<QueryFilter, String, NeverThrowsException>() {
                            @Override
                            public String apply(QueryFilter filter) {
                                return filter.accept(SQLQueryFilterVisitor.this, parameters);
                            }
                        }),
                new StringBuilder(" ").append(operand).append(" ").toString()));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitAndFilter(final P parameters, List<QueryFilter> subFilters) {
        return visitCompositeFilter(parameters, subFilters, "AND");
    }

    @Override
    public String visitBooleanLiteralFilter(P parameters, boolean value) {
        return value ? "1 = 1" : "1 <> 1";
    }

    @Override
    public String visitContainsFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, "LIKE", field, "%" + valueAssertion + "%");
    }

    @Override
    public String visitEqualsFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, "=", field, valueAssertion);
    }

    @Override
    public String visitExtendedMatchFilter(P parameters, JsonPointer field, String operator, Object valueAssertion) {
        throw new UnsupportedOperationException("Extended match filter not supported on this endpoint");
    }

    @Override
    public String visitGreaterThanFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, ">", field, valueAssertion);
    }

    @Override
    public String visitGreaterThanOrEqualToFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, ">=", field, valueAssertion);
    }

    @Override
    public String visitLessThanFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, "<", field, valueAssertion);
    }

    @Override
    public String visitLessThanOrEqualToFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, "<=", field, valueAssertion);
    }

    @Override
    public String visitNotFilter(P parameters, QueryFilter subFilter) {
        return "NOT " + subFilter.accept(this, parameters);
    }

    @Override
    public String visitOrFilter(final P parameters, List<QueryFilter> subFilters) {
        return visitCompositeFilter(parameters, subFilters, "OR");
    }

    @Override
    public abstract String visitPresentFilter(P parameters, JsonPointer field);

    @Override
    public String visitStartsWithFilter(P parameters, JsonPointer field, Object valueAssertion) {
        return visitValueAssertion(parameters, "LIKE", field, valueAssertion + "%");
    }
}
