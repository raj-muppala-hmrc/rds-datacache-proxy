/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories

import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PasPayeSource {
  def createPayeAppeal(request: CreatePayeAppealRequest): Future[CreatePayeAppealResponse]
}

class PasPayeRepository @Inject() (
  @NamedDatabase("pas") db: Database
)(implicit ec: ExecutionContext)
    extends PasPayeSource {

  override def createPayeAppeal(request: CreatePayeAppealRequest): Future[CreatePayeAppealResponse] = {
    Future {
      db.withTransaction { conn =>
        callCreatePayeAppeal(
          conn         = conn,
          p_penalty_id = request.penaltyId
        )
      }
    }
  }

  private def callCreatePayeAppeal(conn: Connection, p_penalty_id: Long): CreatePayeAppealResponse = {

    val cs = conn.prepareCall("{call PAYE_PAS.create_appeal(?)}")

    try {
      cs.setLong(1, p_penalty_id)

      cs.execute()

      CreatePayeAppealResponse(
        created = true
      )

    } finally {
      cs.close()
    }
  }
}
