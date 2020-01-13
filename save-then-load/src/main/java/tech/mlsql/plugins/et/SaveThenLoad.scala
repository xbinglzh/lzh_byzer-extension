package tech.mlsql.plugins.et

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import streaming.core.datasource.impl.MLSQLDelta
import streaming.core.datasource.{DataSinkConfig, DataSourceConfig}
import streaming.dsl.auth.TableAuthResult
import streaming.dsl.mmlib._
import streaming.dsl.mmlib.algs.Functions
import streaming.dsl.mmlib.algs.param.{BaseParams, WowParams}
import tech.mlsql.common.utils.serder.json.JSONTool
import tech.mlsql.dsl.auth.ETAuth
import tech.mlsql.dsl.auth.dsl.mmlib.ETMethod.ETMethod
import tech.mlsql.version.VersionCompatibility

/**
 * 13/1/2020 WilliamZhu(allwefantasy@gmail.com)
 */
class SaveThenLoad(override val uid: String) extends SQLAlg with VersionCompatibility with Functions with WowParams with ETAuth {
  def this() = this(BaseParams.randomUID())

  override def train(df: DataFrame, path: String, params: Map[String, String]): DataFrame = {
    val command = JSONTool.parseJson[List[String]](params("parameters")).toArray
    val session = df.sparkSession
    command match {
      case Array(tableName) =>
        val ds = new MLSQLDelta()
        ds.save(session.table(tableName).write, DataSinkConfig(s"__tmp__.${tableName}", Map(), SaveMode.Overwrite, Option(df)))
        ds.load(session.read, DataSourceConfig(s"__tmp__.${tableName}", Map(), Option(df)))
      case _ => throw new RuntimeException("!saveThenLoad tableName;")
    }
  }

  override def auth(etMethod: ETMethod, path: String, params: Map[String, String]): List[TableAuthResult] = {
    List()
  }

  override def supportedVersions: Seq[String] = {
    Seq("1.5.0-SNAPSHOT", "1.5.0")
  }


  override def doc: Doc = Doc(MarkDownDoc,
    s"""
       |
       |```
       |${codeExample.code}
       |```
    """.stripMargin)


  override def codeExample: Code = Code(SQLCode,
    """
      |example
    """.stripMargin)

  override def batchPredict(df: DataFrame, path: String, params: Map[String, String]): DataFrame = train(df, path, params)

  override def load(sparkSession: SparkSession, path: String, params: Map[String, String]): Any = ???

  override def predict(sparkSession: SparkSession, _model: Any, name: String, params: Map[String, String]): UserDefinedFunction = ???


}