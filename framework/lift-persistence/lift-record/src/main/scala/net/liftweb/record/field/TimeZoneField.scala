/*
 * Copyright 2007-2010 WorldWide Conferencing, LLC
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

package net.liftweb {
package record {
package field {

import scala.xml._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.{S, SHtml}
import _root_.java.util.TimeZone
import S._
import Helpers._

object TimeZoneField {
  lazy val timeZoneList: List[(String, String)] = TimeZone.getAvailableIDs.toList.
    filter(!_.startsWith("SystemV/")).
    filter(!_.startsWith("Etc/")).filter(_.length > 3).
    sort(_ < _).map(tz => (tz, tz))
}

class TimeZoneField[OwnerType <: Record[OwnerType]](rec: OwnerType) extends StringField(rec, 32) {

  override def defaultValue = TimeZone.getDefault.getID

  def isAsTimeZone: TimeZone = TimeZone.getTimeZone(value) match {
    case null => TimeZone.getDefault
    case x => x
  }

  /** Label for the selection item representing Empty, show when this field is optional. Defaults to the empty string. */
  def emptyOptionLabel: String = ""

  def buildDisplayList: List[(String, String)] =
      if (optional_?) ("", emptyOptionLabel)::TimeZoneField.timeZoneList else TimeZoneField.timeZoneList

  private def elem = SHtml.select(buildDisplayList, Full(valueBox openOr ""), set) % ("tabindex" -> tabIndex.toString)

  override def toForm: Box[NodeSeq] = 
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> (id + "_field")))
      case _ => Full(elem)
    }
}

import _root_.java.sql.{ResultSet, Types}
import _root_.net.liftweb.mapper.{DriverType}

class DBTimeZoneField[OwnerType <: Record[OwnerType]](rec: OwnerType) extends TimeZoneField(rec)
  with JDBCFieldFlavor[String] {

  def targetSQLType = Types.VARCHAR

  /**
   * Given the driver type, return the string required to create the column in the database
   */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName+" VARCHAR("+32+")"

  def jdbcFriendly(field : String) : String = value

}

}
}
}
