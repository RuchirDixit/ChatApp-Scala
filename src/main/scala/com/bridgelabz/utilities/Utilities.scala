// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.bridgelabz.utilities

import scala.concurrent.{Await, Future, TimeoutException}
import concurrent.duration._
object Utilities {
  /**
   *
   * @param future Future instance to be awaited
   * @param time timeout Span
   * @tparam T generic object type to be extracted
   * @return result else None in case of exception.
   */
    @Deprecated
  def tryAwait[T](future: Future[T], time: Int): Option[T] = {
    try {
      Some(Await.result(future, time.seconds))
    }
    catch {
      case throwable: TimeoutException => throwable.printStackTrace()
        None
    }
  }
}
