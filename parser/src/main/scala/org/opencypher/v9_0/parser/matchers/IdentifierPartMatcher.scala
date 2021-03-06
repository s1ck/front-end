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
package org.opencypher.v9_0.parser.matchers

class IdentifierPartMatcher extends ScalaCharMatcher("an identifier character") {
  protected def matchChar(c: Char): Boolean = Character.isJavaIdentifierPart(c)
}

class GlobbedIdentifierPartMatcher extends IdentifierPartMatcher {
  override protected def matchChar(c: Char): Boolean = super.matchChar(c) || '*'.equals(c) || '?'.equals(c)
}
