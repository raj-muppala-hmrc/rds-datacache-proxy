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

package uk.gov.hmrc.rdsdatacacheproxy.pas.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreatePayeAppealRequest, CreatePayeAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasPayeSource

import scala.concurrent.Future

final class PasPayeServiceSpec extends SpecBase {

  private val source = mock[PasPayeSource]
  private val service = new PasPayeService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  "PasPayeService.createPayeAppeal" - {

    "return CreatePayeAppealResponse when the repository returns CreatePayeAppealResponse" in {
      val createPayeAppealRequest = CreatePayeAppealRequest(1L)
      val createPayeAppealResponse = CreatePayeAppealResponse(true)

      when(source.createPayeAppeal(createPayeAppealRequest)).thenReturn(Future.successful(createPayeAppealResponse))

      val out = service.createPayeAppeal(createPayeAppealRequest).futureValue

      out mustBe createPayeAppealResponse

      verify(source).createPayeAppeal(eqTo(createPayeAppealRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val createPayeAppealRequest = CreatePayeAppealRequest(1L)
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.createPayeAppeal(createPayeAppealRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .createPayeAppeal(createPayeAppealRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).createPayeAppeal(createPayeAppealRequest)
      verifyNoMoreInteractions(source)
    }
  }
}
