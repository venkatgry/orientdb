/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.sql.functions.coll;

import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.filter.OSQLFilterItem;
import com.orientechnologies.orient.core.sql.functions.OSQLFunction;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionAbstract;

/**
 * Extract the first item of multi values (arrays, collections and maps) or return the same value for non multi-value types.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
public class OSQLFunctionFirst extends OSQLFunctionAbstract {
  public static final String NAME  = "first";

  private Object             first = this;

  public OSQLFunctionFirst() {
    super(NAME, 1, 1);
  }

  public Object execute(final OIdentifiable iCurrentRecord, ODocument iCurrentResult, final Object[] iParameters,
      final OCommandContext iContext) {
    Object value = iParameters[0];

    if (value instanceof OSQLFilterItem)
      value = ((OSQLFilterItem) value).getValue(iCurrentRecord, iContext);

    if (OMultiValue.isMultiValue(value))
      value = OMultiValue.getFirstValue(value);

    if (first == this)
      // ONLY THE FIRST TIME
      first = value;

    return value;
  }

  public boolean aggregateResults() {
    return false;
    //return configuredParameters.length == 1;
  }

  public Object getResult() {
    return first;
  }

  public boolean filterResult() {
    return true;
  }

  public String getSyntax() {
    return "Syntax error: first(<field>)";
  }

  @Override
  public OSQLFunction copy() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object evaluate(OCommandContext context, Object candidate) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
