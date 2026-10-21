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

package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreatePayeAppealRequest, CreatePayeAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.PasPayeService

import scala.concurrent.Future

class PasPayeControllerSpec extends SpecBase with MockitoSugar {

  "createPayeAppeal" - {

    "return 201 with CreatePayeAppealResponse when service succeeds" in new Setup {

      val createPayeAppealRequest = CreatePayeAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createPayeAppealRequest), "/pas/paye/appeal")
      val createPayeAppealResponse: CreatePayeAppealResponse = CreatePayeAppealResponse(true)

      when(mockService.createPayeAppeal(eqTo(createPayeAppealRequest))).thenReturn(Future.successful(createPayeAppealResponse))

      val result: Future[Result] = controller.createPayeAppeal()(request)

      status(result)        shouldBe CREATED
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(createPayeAppealResponse)

      verify(mockService, times(1)).createPayeAppeal(createPayeAppealRequest)
    }

    "return 400 when the payload is invalid" in new Setup {

      val request: FakeRequest[JsValue] = makeJsonRequest(Json.obj(), "/pas/paye/appeal")

      val result: Future[Result] = controller.createPayeAppeal()(request)

      status(result)      shouldBe BAD_REQUEST
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid payload"

      verifyNoInteractions(mockService)
    }

    "return 500 with generic message on unexpected exception" in new Setup {

      val createPayeAppealRequest = CreatePayeAppealRequest(1L)
      val request: FakeRequest[JsValue] = makeJsonRequest(Json.toJson(createPayeAppealRequest), "/pas/paye/appeal")

      when(mockService.createPayeAppeal(eqTo(createPayeAppealRequest))).thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.createPayeAppeal()(request)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService, times(1)).createPayeAppeal(createPayeAppealRequest)
    }
  }

  private trait Setup {
    val mockService: PasPayeService = mock[PasPayeService]
    val controller: PasPayeController = new PasPayeController(fakeAuthAction, mockService, cc)

    def makeJsonRequest(body: JsValue, path: String): FakeRequest[JsValue] =
      FakeRequest(POST, path)
        .withHeaders(CONTENT_TYPE -> JSON, ACCEPT -> JSON)
        .withBody(body)
  }
}
