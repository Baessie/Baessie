package org.baessie.testtools

import java.lang.reflect.Field
import org.apache.tapestry5.services.Environment
import org.apache.tapestry5.ioc.annotations.{InjectService, Inject}
import org.apache.tapestry5.annotations._

object PageTestTool {
  val injectAnnotations: List[_ <: AnyRef] = List(classOf[Inject], classOf[InjectService], classOf[InjectComponent], classOf[InjectContainer], classOf[InjectPage], classOf[Component], classOf[Environment])

  def inject(target: Object, injections: Array[_ <: AnyRef]) = {
    val fields: List[Field] = getAllFields(target.getClass)
    val fieldAnnotations = fields.map(field => (field, field.getDeclaredAnnotations.map(_.annotationType())))
    val annotatedFields = fieldAnnotations.filterNot(_._2.intersect(injectAnnotations).isEmpty).map(_._1)
    val mappedFields: List[(Field, Option[_ <: Any])] = annotatedFields.map(field => {
      (field, injections.find(field.getType().isInstance(_)))
    })
    mappedFields.filter(_._2 isDefined).foreach(mapping => {
      setFieldValue(target, mapping._1, mapping._2.get)
    })
  }

  def setRequestParameters(target: Object, parameters: Map[String, String]) = {
    val fields: List[Field] = getAllFields(target.getClass)
    val annotatedFields = fields.filter(_.getAnnotation(classOf[ActivationRequestParameter]) != null)
    val fieldNames = annotatedFields.map(field => {
      val annotation = field.getAnnotation(classOf[ActivationRequestParameter])
      if (annotation != null && annotation.value() != "") (field, annotation.value()) else (field, field.getName)
    })
    fieldNames.foreach(mapping => {
      setFieldValue(target, mapping._1, parameters(mapping._2))
    })
  }

  private def getAllFields(clazz: Class[_ <: Any]): List[Field] = {
    val classes = getClassHierarchy(clazz, Nil)
    return classes.map(_ getDeclaredFields()).flatten
  }

  private def getClassHierarchy(clazz: Class[_ <: Any], classes: List[Class[_ <: Any]]): List[Class[_ <: Any]] = {
    if (clazz.getSuperclass == null) {
      clazz :: classes
    } else {
      clazz :: getClassHierarchy(clazz.getSuperclass, classes)
    }
  }

  private def setFieldValue(target: Object, field: Field, value: Any) {
    val accessible = field.isAccessible
    field.setAccessible(true)
    field.set(target, value)
    field.setAccessible(accessible)
  }
}
