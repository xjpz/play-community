package services

import javax.inject.{Inject, Singleton}
import cn.playscala.mongo.Mongo
import models.Category
import models.JsonFormats.categoryFormat
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import scala.concurrent.Future

@Singleton
class CatalogService @Inject() (mongo: Mongo) {
  val docCatalogCol = mongo.collection("doc-catalog")
  /**
    * 根据catalogId获取目录名称
    * @param catalogId 目录节点id
    * @return
    */
  def getCatalogName(catalogId: String): Future[String] = {
    for{
      catalogOpt <- docCatalogCol.find().first
    } yield {
      catalogOpt match {
        case Some(obj) =>
          val tree = obj("nodes").as[JsArray]
          val nodes = tree.value.flatMap(preOrder _)
          nodes.find(n => n("id").as[String] == catalogId) match {
            case Some(n) => n("text").as[String]
            case None => "未知目录"
          }

        case None =>
          "未知目录"
      }
    }
  }

  /**
    * 先序遍历目录节点
    * @param node 目录子树
    * @return 目录子树的先序遍历结果
    */
  private def preOrder(node: JsValue): List[JsValue] = {
    val children = (node \ "children").as[JsArray]
    if(children.value.size > 0){
      List(node) ::: children.value.flatMap(preOrder _).toList
    } else {
      List(node)
    }
  }


}
