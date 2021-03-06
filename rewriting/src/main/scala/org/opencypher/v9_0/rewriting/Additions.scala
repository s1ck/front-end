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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.ast.AlterUser
import org.opencypher.v9_0.ast.CreateIndex
import org.opencypher.v9_0.ast.CreateNodeKeyConstraint
import org.opencypher.v9_0.ast.CreateNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateUniquePropertyConstraint
import org.opencypher.v9_0.ast.CreateUser
import org.opencypher.v9_0.ast.DatabasePrivilege
import org.opencypher.v9_0.ast.DbmsPrivilege
import org.opencypher.v9_0.ast.DenyPrivilege
import org.opencypher.v9_0.ast.DropConstraintOnName
import org.opencypher.v9_0.ast.DropIndexOnName
import org.opencypher.v9_0.ast.GrantPrivilege
import org.opencypher.v9_0.ast.GraphPrivilege
import org.opencypher.v9_0.ast.HomeDatabaseScope
import org.opencypher.v9_0.ast.HomeGraphScope
import org.opencypher.v9_0.ast.IfExistsDoNothing
import org.opencypher.v9_0.ast.RevokePrivilege
import org.opencypher.v9_0.ast.SetUserHomeDatabaseAction
import org.opencypher.v9_0.ast.ShowConstraints
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.ShowIndexesClause
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.UnresolvedCall
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.expressions.ExistsSubClause
import org.opencypher.v9_0.util.CypherExceptionFactory

object Additions {

  // This is functionality that has been added earlier in 4.x and should not work when using CYPHER 3.5
  case object addedFeaturesIn4_x extends Additions {

    override def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = statement.treeExists {

      case u: UseGraph =>
        throw cypherExceptionFactory.syntaxException("The USE clause is not supported in this Cypher version.", u.position)

      // CREATE INDEX [name] [IF NOT EXISTS] FOR (n:Label) ON (n.prop) [OPTIONS {...}]
      case c: CreateIndex =>
        throw cypherExceptionFactory.syntaxException("Creating index using this syntax is not supported in this Cypher version.", c.position)

      // DROP INDEX name
      case d: DropIndexOnName =>
        throw cypherExceptionFactory.syntaxException("Dropping index by name is not supported in this Cypher version.", d.position)

      // CREATE CONSTRAINT name ON ... IS NODE KEY
      case c@CreateNodeKeyConstraint(_, _, _, Some(_), _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named node key constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON ... IS NODE KEY
      case c@CreateNodeKeyConstraint(_, _, _, _, IfExistsDoNothing, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating node key constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT ... IS NODE KEY OPTIONS {...}
      case c@CreateNodeKeyConstraint(_, _, _, _, _, options, _) if options.nonEmpty =>
        throw cypherExceptionFactory.syntaxException("Creating node key constraint with options is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON ... IS UNIQUE
      case c@CreateUniquePropertyConstraint(_, _, _, Some(_),_, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named uniqueness constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON ... IS UNIQUE
      case c@CreateUniquePropertyConstraint(_, _, _, _, IfExistsDoNothing, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating uniqueness constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT ... IS UNIQUE OPTIONS {...}
      case c@CreateUniquePropertyConstraint(_, _, _, _, _, options, _) if options.nonEmpty =>
        throw cypherExceptionFactory.syntaxException("Creating uniqueness constraint with options is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON () ... EXISTS
      case c@CreateNodePropertyExistenceConstraint(_, _, _, Some(_), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named node existence constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON () ... EXISTS
      case c@CreateNodePropertyExistenceConstraint(_, _, _, _, IfExistsDoNothing, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating node existence constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON ()-[]-() ... EXISTS
      case c@CreateRelationshipPropertyExistenceConstraint(_, _, _, Some(_), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named relationship existence constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON ()-[]-() ... EXISTS
      case c@CreateRelationshipPropertyExistenceConstraint(_, _, _, _, IfExistsDoNothing, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating relationship existence constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // DROP CONSTRAINT name
      case d: DropConstraintOnName =>
        throw cypherExceptionFactory.syntaxException("Dropping constraint by name is not supported in this Cypher version.", d.position)

      case e: ExistsSubClause =>
        throw cypherExceptionFactory.syntaxException("Existential subquery is not supported in this Cypher version.", e.position)

      // SHOW [ALL|BTREE] INDEX[ES] [BRIEF|VERBOSE|WHERE clause|YIELD clause]
      case s: ShowIndexesClause =>
        throw cypherExceptionFactory.syntaxException("SHOW INDEXES is not supported in this Cypher version.", s.position)

      // SHOW [ALL|UNIQUE|NODE EXIST[S]|RELATIONSHIP EXIST[S]|EXIST[S]|NODE KEY] CONSTRAINT[S] [BRIEF|VERBOSE[OUTPUT]]
      case s: ShowConstraints =>
        throw cypherExceptionFactory.syntaxException("SHOW CONSTRAINTS is not supported in this Cypher version.", s.position)

      // Administration commands against system database are not supported at all in CYPHER 3.5.
      // This is checked in CompilerFactory, so separate checks for such commands are not needed here.
    }
  }

  // This is functionality that has been added in 4.3 and should not work when using CYPHER 3.5 and CYPHER 4.2
  case object addedFeaturesIn4_3 extends Additions {

    override def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = statement.treeExists {

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] ON (node:Label) ASSERT node.prop IS NOT NULL
      case c: CreateNodePropertyExistenceConstraint if !c.oldSyntax =>
        throw cypherExceptionFactory.syntaxException("Creating node existence constraint using `IS NOT NULL` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] ON ()-[r:R]-() ASSERT r.prop IS NOT NULL
      case c: CreateRelationshipPropertyExistenceConstraint if !c.oldSyntax =>
        throw cypherExceptionFactory.syntaxException("Creating relationship existence constraint using `IS NOT NULL` is not supported in this Cypher version.", c.position)

      case c: CreateUser if c.userOptions.homeDatabase.isDefined =>
        throw cypherExceptionFactory.syntaxException("Creating a user with a home database is not supported in this Cypher version.", c.position)

      case c: AlterUser if c.userOptions.homeDatabase.isDefined =>
        throw cypherExceptionFactory.syntaxException("Updating a user with a home database is not supported in this Cypher version.", c.position)

      case c: UnresolvedCall if c.yieldAll =>
        throw cypherExceptionFactory.syntaxException("Procedure call using `YIELD *` is not supported in this Cypher version.", c.position)

      case c: AlterUser if c.ifExists =>
        throw cypherExceptionFactory.syntaxException("Updating a user with `IF EXISTS` is not supported in this Cypher version.", c.position)

      case c:ShowIndexesClause if c.where.isDefined || c.hasYield =>
        throw cypherExceptionFactory.syntaxException("Using YIELD or WHERE to list indexes is not supported in this Cypher version.", c.position)

      case c: ShowDatabase if c.scope.isInstanceOf[HomeDatabaseScope] =>
        throw cypherExceptionFactory.syntaxException("`SHOW HOME DATABASE` is not supported in this Cypher version.", c.position)

      // GRANT/DENY/REVOKE SET HOME DATABASE ...
      case p@GrantPrivilege(DbmsPrivilege(SetUserHomeDatabaseAction), _, _, _) =>
        throw cypherExceptionFactory.syntaxException("SET USER HOME DATABASE privilege is not supported in this Cypher version.", p.position)
      case p@DenyPrivilege(DbmsPrivilege(SetUserHomeDatabaseAction), _, _, _)   =>
        throw cypherExceptionFactory.syntaxException("SET USER HOME DATABASE privilege is not supported in this Cypher version.", p.position)
      case p@RevokePrivilege(DbmsPrivilege(SetUserHomeDatabaseAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("SET USER HOME DATABASE privilege is not supported in this Cypher version.", p.position)

      // GRANT/DENY/REVOKE ... ON HOME DATABASE ...
      case p@GrantPrivilege(DatabasePrivilege(_, List(HomeDatabaseScope())), _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Granting privileges on `HOME DATABASE` is not supported in this Cypher version.", p.position)
      case p@DenyPrivilege(DatabasePrivilege(_, List(HomeDatabaseScope())), _, _, _)      =>
        throw cypherExceptionFactory.syntaxException("Denying privileges on `HOME DATABASE` is not supported in this Cypher version.", p.position)
      case p@RevokePrivilege(DatabasePrivilege(_, List(HomeDatabaseScope())), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Revoking privileges on `HOME DATABASE` is not supported in this Cypher version.", p.position)

      // GRANT/DENY/REVOKE ... ON HOME GRAPH ...
      case p@GrantPrivilege(GraphPrivilege(_, List(HomeGraphScope())), _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Granting privileges on `HOME GRAPH` is not supported in this Cypher version.", p.position)
      case p@DenyPrivilege(GraphPrivilege(_, List(HomeGraphScope())), _, _, _)   =>
        throw cypherExceptionFactory.syntaxException("Denying privileges on `HOME GRAPH` is not supported in this Cypher version.", p.position)
      case p@RevokePrivilege(GraphPrivilege(_, List(HomeGraphScope())), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Revoking privileges on `HOME GRAPH` is not supported in this Cypher version.", p.position)
    }
  }

}

trait Additions extends {
  def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = {}
}
