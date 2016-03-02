package org.template.recommendation

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.storage.Storage

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import org.apache.spark.h2o._

import grizzled.slf4j.Logger

case class DataSourceParams(appId: Int) extends Params
 
class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val eventsDb = Storage.getPEvents()
    val eventsRDD: RDD[Event] = eventsDb.find(
      appId = dsp.appId,
      entityType = Some("jobs"),
      eventNames = Some(List("$set")))(sc)

    val jobEntityRDD: RDD[JobEntity] = eventsRDD.map { event =>
      val jobEntity: JobEntity = 
        event.event match {
          case "$set" => 
            JobEntity(
                jobTitle = event.properties.get[String]("job_title"),
                category = event.properties.get[String]("category")
              )
          case _ => throw new Exception(s"Unexpected event ${event} is read.")
        }
        jobEntity
    }.cache()

    new TrainingData(jobEntityRDD)
  }
}

case class JobEntity(
  jobTitle: String,
  category: String,
)

class TrainingData(
  val jobEntities: RDD[JobEntity]
) extends Serializable /* {
  override def toString = {
    s"electricalLoads: [${electricalLoads.count()}] (${electricalLoads.take(2).toList}...)"
  }
} */
