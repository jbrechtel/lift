/*
 * Copyright 2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb
package squerylrecord

import common.{Box, Full, Loggable}
import mapper.DB // FIXME should be moved out of mapper
import util.DynoVar

import org.squeryl.{Session, SessionFactory}
import org.squeryl.internals.{DatabaseAdapter, FieldMetaData}

/** Object containing initialization logic for the Squeryl/Record integration */
object SquerylRecord extends Loggable {
  /** Keep track of the current Squeryl Session we've created using DB */
  private object currentSession extends DynoVar[Session]
  
  /**
   * We have to remember the default Squeryl metadata factory before
   * we override it with our own implementation, so that we can use
   * the original factory for non-record classes.
   */
  private[squerylrecord] var posoMetaDataFactory = FieldMetaData.factory
  
  /**
   * Initialize the Squeryl/Record integration. This must be called somewhere during your Boot, and before you use any
   * Records with Squeryl. Use this function instead of init if you want to use the squeryl session factory
   * instead of mapper.DB as the transaction manager with squeryl-record.
   */
  def initWithSquerylSession(sessionFactory: => Session) {
    FieldMetaData.factory = new RecordMetaDataFactory
    SessionFactory.concreteFactory = Some(() => sessionFactory)
  }
  
  /**
   * Initialize the Squeryl/Record integration. This must be called somewhere during your Boot, and before you use any
   * Records with Squeryl. Use this function if you want to use mapper.DB as the transaction manager
   * with squeryl-record.
   */
  def init(mkAdapter: () => DatabaseAdapter) = {
    FieldMetaData.factory = new RecordMetaDataFactory
    SessionFactory.externalTransactionManagementAdapter = Some(() => currentSession.is openOr {
      DB.currentConnection match {
        case Full(superConn) =>
          val sess = Session.create(superConn.connection, mkAdapter())
          sess.setLogger(s => logger.debug(s))
          currentSession.set(sess)
          sess

        case _ => error("no current connection in scope. wrap your transaction with DB.use or use one of the DB loan wrappers")
      }
    })
  }
}
