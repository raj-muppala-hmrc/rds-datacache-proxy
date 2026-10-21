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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreatePayeAppealRequest, CreatePayeAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasPayeSource
import uk.gov.hmrc.rdsdatacacheproxy.pas.stub.PasPayeStubData

import scala.concurrent.Future

class PasPayeControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class PasPayeStub extends PasPayeSource {
    override def createPayeAppeal(request: CreatePayeAppealRequest): Future[CreatePayeAppealResponse] =
      Future.successful(
        PasPayeStubData.makeCreatePayeAppealResponse(request)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides {
        bind[PasPayeSource].toInstance(new PasPayeStub())
      }
      .build()

  private final val endpoint = "/pas"

  "POST /pas/paye/appeal" should {

    "return 201 with a valid CreatePayeAppealResponse" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/paye/appeal", requestJson).futureValue

      response.status mustBe CREATED
      response.contentType mustBe "application/json"

      response.json.as[CreatePayeAppealResponse] mustBe PasPayeStubData.createPayeAppealSuccessResponse
    }

    "return 400 when the payload is invalid" in {
      AuthStub.authorised()

      val response = post(s"$endpoint/paye/appeal", Json.obj()).futureValue

      response.status mustBe BAD_REQUEST
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 500L
      )
      val response = post(s"$endpoint/paye/appeal", requestJson).futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/paye/appeal", requestJson).futureValue

      response.status mustBe UNAUTHORIZED
    }
  }
}
