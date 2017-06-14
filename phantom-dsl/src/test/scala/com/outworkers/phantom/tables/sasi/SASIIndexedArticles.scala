/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.tables.sasi

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Article

abstract class SASIIndexedArticles extends Table[SASIIndexedArticles, Article] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn

  object orderId extends LongColumn with SASIIndex[Mode.Prefix] {
    override def analyzer: StandardAnalyzer[Mode.Prefix] = Analyzer.StandardAnalyzer[Mode.Prefix]().enableStemming(true)
  }
}


case class MultiSASIRecord(
  id: UUID,
  name: String,
  description: String,
  set: Set[Int],
  list: List[String]
)

abstract class MultiSASITable extends Table[MultiSASITable, MultiSASIRecord] {
  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn with SASIIndex[Mode.Contains] {
    override def analyzer: NonTokenizingAnalyzer[Mode.Contains] = {
      Analyzer.NonTokenizingAnalyzer[Mode.Contains]().normalizeLowercase(true)
    }
  }

  object description extends StringColumn with SASIIndex[Mode.Sparse] {
    override def analyzer: StandardAnalyzer[Mode.Sparse] = {
      Analyzer.StandardAnalyzer[Mode.Sparse]().skipStopWords(true).enableStemming(true)
    }
  }

  object set extends SetColumn[Int]
  object list extends ListColumn[String]
}