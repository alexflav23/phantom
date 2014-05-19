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
package com.newzly.phantom.dsl.query

import org.scalatest.{FeatureSpec, ParallelTestExecution, FlatSpec, Matchers}
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables._
import com.newzly.util.testing.Sampler

class QueryRestrictionsTest extends FeatureSpec with Matchers with ParallelTestExecution {


  feature("Restricting type signatures") {

    scenario("allow using a correct type for a value method") {
      "Primitives.insert.value(_.boolean, true)" should compile
    }

    scenario("not allow using a wrong type for a value method") {
      "Primitives.insert.value(_.boolean, 5)" shouldNot compile
    }
  }

  feature("Restricting index operators on non-index columns") {

    scenario("allow using the eqs operator on index columns") {
      "Primitives.select.where(_.pkey eqs Sampler.getARandomString)" should compile
    }

    scenario("not allow using the eqs operator on non index columns") {
      "Primitives.select.where(_.long eqs 5L)" shouldNot compile
    }

    scenario("allow using the lt operator on index columns") {
      "Primitives.select.where(_.pkey lt Sampler.getARandomString)" should compile
    }

    scenario("not allow using the lt operator on non index columns") {
      "Primitives.select.where(_.long lt 5L)" shouldNot compile
    }

    scenario("allow using the lte operator on index columns") {
      "Primitives.select.where(_.pkey lte Sampler.getARandomString)" should compile
    }

    scenario("not allow using the lte operator on non index columns") {
      "Primitives.select.where(_.long lte 5L)" shouldNot compile
    }

    scenario("allow using the gt operator on index columns") {
      "Primitives.select.where(_.pkey gt Sampler.getARandomString)" should compile
    }

    scenario("not allow using the gt operator on non index columns") {
      "Primitives.select.where(_.long gt 5L)" shouldNot compile
    }

    scenario("allow using the gte operator on index columns") {
      "Primitives.select.where(_.pkey gte Sampler.getARandomString)" should compile
    }

    scenario("not allow using the gte operator on non index columns") {
      "Primitives.select.where(_.long gte 5L)" shouldNot compile
    }

    scenario("allow using the in operator on index columns") {
      "Primitives.select.where(_.pkey in List(Sampler.getARandomString, Sampler.getARandomString))" should compile
    }

    scenario("not allow using the in operator on non index columns") {
      "Primitives.select.where(_.long in List(5L, 6L))" shouldNot compile
    }
  }


  feature("Restricting modify operators on index columns") {

    scenario("not allow using the setTo operator on a Counter column") {
      "CounterTableTest.update.where(_.id eqs UUIDs.timeBased()).modify(_.count_entries setTo 5L)" shouldNot compile
    }

    scenario("not allow using the setTo operator on a PartitionKey") {
      "CounterTableTest.update.where(_.id eqs UUIDs.timeBased()).modify(_.id setTo UUIDs.timeBased())" shouldNot compile
    }

    scenario("not allow using the setTo operator on a PrimaryKey") {
      "TwoKeys.update.where(_.pkey eqs UUIDs.timeBased().toString).modify(_.pkey setTo UUIDs.timeBased().toString)" shouldNot compile
    }

    scenario("allow using setTo operators for non index columns") {
      """TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo "test")""" shouldNot compile
    }

    scenario("not allow using the setTo operator on a Clustering column") {
      "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.timestamp setTo new DateTime)" shouldNot compile
    }
  }

  feature("Restrict using certain queries in batches") {
    scenario("not allow using Select queries in a batch") {
      "BatchStatement().add(Primitives.select)" shouldNot compile
    }

    scenario("not allow using a primary key in a conditional clause") {
      """Recipes.update.where(_.url eqs "someUrl").modify(_.name setTo "test").onlyIf(_.id eqs secondary)""" shouldNot compile
    }

    scenario("not allow using SelectWhere queries in a batch") {
      "BatchStatement().add(Primitives.select.where(_.pkey eqs Sampler.getARandomString))" shouldNot compile
    }

    scenario("not allow using Truncate queries in a batch") {
      "BatchStatement().add(Primitives.truncate)" shouldNot compile
    }

    scenario("not allow using Create queries in a batch") {
      "BatchStatement().add(Primitives.create)" shouldNot compile
    }
  }

  feature("Allow the right queries in a Batch") {
    scenario("Insert queries are batchable") {
      "BatchStatement().add(Primitives.insert)" should compile
    }

    scenario("Insert.Value queries should be batchable") {
      "BatchStatement().add(Primitives.insert.value(_.long, 4L))" should compile
    }

    scenario("Update.Assignments queries should be batchable") {
      "BatchStatement().add(Primitives.update.modify(_.long setTo 5L))" should compile
    }

    scenario("Update.Where queries should be batchable") {
      "BatchStatement().add(Primitives.update.where(_.pkey eqs Sampler.getARandomString))" should compile
    }

    scenario("Conditional Update.Where queries should be batchable") {
      "BatchStatement().add(Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L))" should compile
    }

    scenario("Conditional Assignments queries should be batchable") {
      "BatchStatement().add(Primitives.update.where(_.pkey eqs Sampler.getARandomString).modify(_.long setTo 10L).onlyIf(_.long eqs 5L))" should compile
    }

    scenario("Delete queries should be batchable") {
      "BatchStatement().add(Primitives.delete)" should compile
    }

    scenario("Delete.Where queries should be batchable") {
      "BatchStatement().add(Primitives.delete)" should compile
    }


  }


}
