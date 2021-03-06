/*
 * Copyright (c) Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.rewriting.ListStepAccumulator
import org.opencypher.v9_0.rewriting.RewriterStep
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.AddUniquenessPredicates
import org.opencypher.v9_0.rewriting.rewriters.InnerVariableNamer
import org.opencypher.v9_0.rewriting.rewriters.ProjectionClausesHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.desugarMapProjection
import org.opencypher.v9_0.rewriting.rewriters.expandStar
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.rewriting.rewriters.foldConstants
import org.opencypher.v9_0.rewriting.rewriters.inlineNamedPathsInPatternComprehensions
import org.opencypher.v9_0.rewriting.rewriters.moveWithPastMatch
import org.opencypher.v9_0.rewriting.rewriters.nameAllPatternElements
import org.opencypher.v9_0.rewriting.rewriters.normalizeArgumentOrder
import org.opencypher.v9_0.rewriting.rewriters.normalizeComparisons
import org.opencypher.v9_0.rewriting.rewriters.normalizeExistsPatternExpressions
import org.opencypher.v9_0.rewriting.rewriters.normalizeHasLabelsAndHasType
import org.opencypher.v9_0.rewriting.rewriters.normalizeMatchPredicates
import org.opencypher.v9_0.rewriting.rewriters.normalizeNotEquals
import org.opencypher.v9_0.rewriting.rewriters.normalizeSargablePredicates
import org.opencypher.v9_0.rewriting.rewriters.parameterValueTypeReplacement
import org.opencypher.v9_0.rewriting.rewriters.projectNamedPaths
import org.opencypher.v9_0.rewriting.rewriters.replaceLiteralDynamicPropertyLookups
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.AccumulatedSteps
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.symbols.CypherType

class ASTRewriter(innerVariableNamer: InnerVariableNamer) {

  private val AccumulatedSteps(orderedSteps, _) = StepSequencer(ListStepAccumulator[StepSequencer.Step with ASTRewriterFactory]()).orderSteps(Set(
    expandStar,
    normalizeHasLabelsAndHasType,
    desugarMapProjection,
    moveWithPastMatch,
    normalizeComparisons,
    foldConstants,
    normalizeExistsPatternExpressions,
    nameAllPatternElements,
    normalizeMatchPredicates,
    normalizeNotEquals,
    normalizeArgumentOrder,
    normalizeSargablePredicates,
    AddUniquenessPredicates,
    replaceLiteralDynamicPropertyLookups,
    inlineNamedPathsInPatternComprehensions,
    parameterValueTypeReplacement,
    projectNamedPaths,
  ), initialConditions = Set(ProjectionClausesHaveSemanticInfo, PatternExpressionsHaveSemanticInfo))

  def rewrite(statement: Statement,
              semanticState: SemanticState,
              parameterTypeMapping: Map[String, CypherType],
              cypherExceptionFactory: CypherExceptionFactory): Statement = {
    val rewriters = orderedSteps.map { step =>
      val rewriter = step.getRewriter(innerVariableNamer, semanticState, parameterTypeMapping, cypherExceptionFactory)
      RewriterStep.validatingRewriter(rewriter, step)
    }

    val combined = inSequence(rewriters: _*)

    statement.endoRewrite(combined)
  }
}
