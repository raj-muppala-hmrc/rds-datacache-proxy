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

package uk.gov.hmrc.rdsdatacacheproxy.pas.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class PasPayeModelsSpec extends AnyWordSpec with Matchers {

  "CreatePayeAppealRequest" should {

    "serialize to JSON correctly" in {
      val request = CreatePayeAppealRequest(
        penaltyId = 1L
      )

      val json = Json.toJson(request)

      (json \ "penaltyId").as[Long] mustBe 1L
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "penaltyId" -> 1L
      )

      val result = json.validate[CreatePayeAppealRequest]

      result mustBe a[JsSuccess[_]]
      result.get.penaltyId mustBe 1L
    }

    "fail to deserialize when penaltyId is missing" in {
      val json = Json.obj()

      val result = json.validate[CreatePayeAppealRequest]

      result.isError mustBe true
    }
  }

  "CreatePayeAppealResponse" should {

    "serialize to JSON correctly when created is true" in {
      val json = Json.toJson(CreatePayeAppealResponse(created = true))

      (json \ "created").as[Boolean] mustBe true
    }

    "serialize to JSON correctly when created is false" in {
      val json = Json.toJson(CreatePayeAppealResponse(created = false))

      (json \ "created").as[Boolean] mustBe false
    }

    "deserialize from JSON correctly" in {
      val result = Json.obj("created" -> true).validate[CreatePayeAppealResponse]

      result mustBe a[JsSuccess[_]]
      result.get.created mustBe true
    }

    "fail to deserialize when created field is missing" in {
      val result = Json.obj().validate[CreatePayeAppealResponse]

      result.isError mustBe true
    }
  }
}
