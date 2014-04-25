package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.util.testing.cassandra.BaseTest
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables._
import com.newzly.util.testing.AsyncAssertionsHelper._

class InsertTest extends BaseTest {
  val keySpace: String = "InsertTestKeySpace"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Primitives.insertSchema()
      TestTable.insertSchema()
      MyTest.insertSchema()
    }
  }

  "Insert" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = Primitive.sample
    val rcp =  Primitives.insert
        .value(_.pkey, row.pkey)
        .value(_.long, row.long)
        .value(_.boolean, row.boolean)
        .value(_.bDecimal, row.bDecimal)
        .value(_.double, row.double)
        .value(_.float, row.float)
        .value(_.inet, row.inet)
        .value(_.int, row.int)
        .value(_.date, row.date)
        .value(_.uuid, row.uuid)
        .value(_.bi, row.bi)
        .future() flatMap {
          _ => {
            for {
              one <- Primitives.select.where(_.pkey eqs row.pkey).one
              multi <- Primitives.select.fetch
            } yield (one.get === row, multi contains row)
          }
       }

    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  "Insert" should "work fine for primitives columns with twitter futures" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = Primitive.sample
    val rcp =  Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)
      .execute() flatMap {
      _ => {
        for {
          one <- Primitives.select.where(_.pkey eqs row.pkey).get
          multi <- Primitives.select.collect()
        } yield (one.get === row, multi contains row)
      }
    }

    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with List, Set, Map" in {
    val row = TestRow.sample()

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .future() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs row.key).one
          multi <- TestTable.select.fetch
        }  yield (one.get === row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with List, Set, Map and Twitter futures" in {
    val row = TestRow.sample()

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .execute() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs row.key).get
          multi <- TestTable.select.collect()
        }  yield (one.get === row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with Mix" in {
    val r = Recipe.sample
    val rcp = Recipes.insert
        .value(_.url, r.url)
        .valueOrNull(_.description, r.description)
        .value(_.ingredients, r.ingredients)
        .valueOrNull(_.servings, r.servings)
        .value(_.last_checked_at, r.lastCheckedAt)
        .value(_.props, r.props)
        .value(_.uid, UUIDs.timeBased()).future() flatMap {
        _ => {
         Recipes.select.one
        }
      }

    rcp successful {
      res => {
        assert (res.get === r)
      }
    }
  }

  it should "work fine with Mix and Twitter futures" in {
    val r = Recipe.sample
    val rcp = Recipes.insert
      .value(_.url, r.url)
      .valueOrNull(_.description, r.description)
      .value(_.ingredients, r.ingredients)
      .valueOrNull(_.servings, r.servings)
      .value(_.last_checked_at, r.lastCheckedAt)
      .value(_.props, r.props)
      .value(_.uid, UUIDs.timeBased()).execute() flatMap {
      _ => {
        Recipes.select.where(_.url eqs r.url).get
      }
    }

    rcp successful {
      res => {
        assert (res.get === r)
      }
    }
  }

  it should "support serializing/de-serializing empty lists " in {
    val row = MyTestRow.sample
    val f = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, List.empty[String])
      .future() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).one
    }

    f successful  {
      res =>
        res.isEmpty shouldEqual false
        res.get.stringlist.isEmpty shouldEqual true
    }
  }

  it should "support serializing/de-serializing empty lists with Twitter futures" in {
    val row = MyTestRow.sample

    val f = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, List.empty[String])
      .execute() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).get
    }

    f successful  {
      res =>
        res.isEmpty shouldEqual false
        res.get.stringlist.isEmpty shouldEqual true
    }
  }

  it should "support serializing/de-serializing to List " in {
    val row = MyTestRow.sample

    val recipeF = MyTest.insert
      .value(_.key, row.key)
      .value(_.optionA, row.optionA)
      .value(_.stringlist, row.stringlist)
      .future() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).one
    }

    recipeF successful  {
      res => {
        res.isEmpty shouldEqual false
        res.get should be(row)
      }
    }
  }

  it should "support serializing/de-serializing to List with Twitter futures" in {
    val row = MyTestRow.sample

    val recipeF = MyTest.insert
      .value(_.key, row.key)
      .value(_.optionA, row.optionA)
      .value(_.stringlist, row.stringlist)
      .execute() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).get
    }

    recipeF successful  {
      res => {
        res.isEmpty shouldEqual false
        res.get should be(row)
      }
    }
  }

}
