/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.gearpump.akkastream.task

import java.time.Instant

import org.apache.gearpump.Message
import org.apache.gearpump.cluster.UserConfig
import org.apache.gearpump.streaming.task.TaskContext

class StatefulMapConcatTask[IN, OUT](context: TaskContext, userConf : UserConfig)
  extends GraphTask(context, userConf) {

  val func = userConf.getValue[() => IN => Iterable[OUT]](StatefulMapConcatTask.FUNC).get
  var f: IN => Iterable[OUT] = _

  override def onStart(startTime: Instant) : Unit = {
    f = func()
  }

  override def onNext(msg : Message) : Unit = {
    val in: IN = msg.msg.asInstanceOf[IN]
    val out: Iterable[OUT] = f(in)
    val iterator = out.iterator
    while(iterator.hasNext) {
      val nextValue = iterator.next
      context.output(Message(nextValue, System.currentTimeMillis()))
    }
  }
}

object StatefulMapConcatTask {
  val FUNC = "FUNC"
}
