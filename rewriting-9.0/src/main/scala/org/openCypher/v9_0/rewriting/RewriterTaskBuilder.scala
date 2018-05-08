/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.openCypher.v9_0.rewriting

import org.openCypher.v9_0.util.Rewriter

case object RewriterTaskBuilder {

  private case class State(conditions: Set[RewriterCondition] = Set.empty,
                           previousName: Option[String] = None,
                           tasks: Seq[RewriterTask] = Seq.empty) {
    def +(name: String, rewriter: Rewriter) =
      copy(previousName = Some(name), tasks = allTasks :+ RunRewriter(name, rewriter))
    def +(condition: RewriterCondition) = copy(conditions = conditions + condition)
    def -(condition: RewriterCondition) = copy(conditions = conditions - condition)
    def allTasks = if (conditions.isEmpty) tasks else tasks :+ RunConditions(previousName, conditions)
  }

  def apply(steps: Seq[RewriterStep]): Seq[RewriterTask] = steps.foldLeft(State()) {
    case (state, ApplyRewriter(name, rewriter)) => state +(name, rewriter)
    case (state, EnableRewriterCondition(condition)) => state + condition
    case (state, DisableRewriterCondition(condition)) => state - condition
  }.allTasks
}
