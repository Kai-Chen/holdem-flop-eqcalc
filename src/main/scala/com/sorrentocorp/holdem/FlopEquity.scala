package com.sorrentocorp.holdem

import org.apache.spark.sql._, functions._

import org.tresamigos.smv._

import com.sorrentocorp.cards._
import com.sorrentocorp.poker.FlopGame
import com.sorrentocorp.poker.Hand

/** Generates hand-vs-hand flop equity using spark */
object FlopEquity {
  def generate (sess: SparkSession, flopstr: String): DataFrame = {
    val board = FlopGame.conform(Hand.from(flopstr))
    val deck = Deck.strip(board)
    val hands: Seq[(String, String)] = for {
      c4 <- deck.cards.combinations(4).toSeq
      t = c4.grouped(2).toSeq.map(_.sorted.reverse.mkString(" "))
    } yield (t(0), t(1))

    val r1 = sess.createDataFrame(hands).smvRenameField("_1"->"hero", "_2"->"opponent")

    // val holecards = SmvApp.app.runModuleByName(input.HoleCards.fqn)
    // val flop = SmvApp.app.runModuleByName(input.Flops.fqn)

    // // get hole card id
    // val r2 = r1.join(holecards.as("a"), col("a.cards") === r1("hero")).
    //   smvSelectMinus("cards").smvRenameField("id" -> "hero_id")
    // val r3 = r2.join(holecards.as("b"), col("b.cards") === r2("opponent")).
    //   smvSelectMinus("cards").smvRenameField("id" -> "opponent_id")

    val args = struct(col("hero"), col("opponent"), lit(board.toString))
    r1.withColumn("equity", equityFun(args))
  }

  val equityFun = udf { (r: Row) =>
    val hero = Hand.from(r.getString(0))
    val opponent = Hand.from(r.getString(1))
    val flop = Hand.from(r.getString(2))
    Calculator.equity(flop, hero, opponent).equity
  }

}
