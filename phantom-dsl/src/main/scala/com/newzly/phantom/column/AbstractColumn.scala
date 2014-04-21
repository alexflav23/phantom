/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.column

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.newzly.phantom.CassandraWrites
import com.newzly.phantom.query.QueryCondition

trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] extends CassandraWrites[T] {

  private[phantom] val isPrimary = false
  private[phantom] val isSecondaryKey = false
  private[phantom] val isPartitionKey = false
  private[phantom] val isCounterColumn = false
  private[phantom] val isStaticColumn = false

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")
}

