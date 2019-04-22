package com.github.tashoyan.visitor.recommender

import org.apache.spark.sql.functions.{col, max, min}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object VisitGraphBuilderMain extends VisitGraphBuilderArgParser {

  val betaPlacePlace: Double = 1.0
  val betaCategoryPlace: Double = 1.0
  val betaPersonPlace: Double = 0.5
  val betaPersonCategory: Double = 0.5

  def main(args: Array[String]): Unit = {
    parser.parse(args, VisitGraphBuilderConfig()) match {
      case Some(config) => doMain(config)
      case None => sys.exit(1)
    }
  }

  private def doMain(implicit config: VisitGraphBuilderConfig): Unit = {
    implicit val spark: SparkSession = SparkSession.builder()
      .getOrCreate()

    val locationVisits = spark.read
      .parquet(s"${config.samplesDir}/location_visits_sample")
    val places = spark.read
      .parquet(s"${config.samplesDir}/places_sample")

    val placeVisits = PlaceVisits.calcPlaceVisits(locationVisits, places)
      .cache()
    //    printPlaceVisits(placeVisits)
    writePlaceVisits(placeVisits)

    val graphBuilder = new VisitGraphBuilder(
      betaPlacePlace,
      betaCategoryPlace,
      betaPersonPlace,
      betaPersonCategory
    )
    val visitGraph = graphBuilder.buildVisitGraph(placeVisits)
    writeVisitGraph(visitGraph)
  }

  private def writeVisitGraph(visitGraph: DataFrame)(implicit config: VisitGraphBuilderConfig): Unit = {
    visitGraph.write
      .partitionBy("region_id")
      .mode(SaveMode.Overwrite)
      .parquet(s"${config.samplesDir}/visit_graph")
  }

  def printPlaceVisits(placeVisits: DataFrame): Unit = {
    println(s"Place visits count: ${placeVisits.count()}")
    placeVisits
      .select(min("timestamp"), max("timestamp"))
      .show(false)
    println("Place visits counts by region:")
    placeVisits
      .groupBy("region_id")
      .count()
      .show(false)
    val visitorsCount = placeVisits
      .select("person_id")
      .distinct()
      .count()
    println(s"Visitors total count: $visitorsCount")
    val topN = 10
    println(s"Top $topN visitors:")
    placeVisits
      .groupBy("person_id")
      .count()
      .orderBy(col("count").desc)
      .limit(topN)
      .show(false)
    println("Place visits sample:")
    placeVisits.show(false)
  }

  private def writePlaceVisits(placeVisits: DataFrame)(implicit config: VisitGraphBuilderConfig): Unit = {
    placeVisits
      .write
      .partitionBy("region_id", "year_month")
      .mode(SaveMode.Overwrite)
      .parquet(s"${config.samplesDir}/place_visits")
  }

}
