/*
 * Copyright 2001-2008 Artima, Inc.
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
package org.scalatest.exceptions

import org.scalatest._

// TODO: A test and code for null labels throwing an NPE
/**
 * Exception that indicates a ScalaCheck property check failed.
 *
 * @param messageFun a function that returns a detail message (not optional) for this <code>GeneratorDrivenPropertyCheckFailedException</code>.
 * @param cause an optional cause, the <code>Throwable</code> that caused this <code>GeneratorDrivenPropertyCheckFailedException</code> to be thrown.
 * @param failedCodeStackDepthFun a function that returns the depth in the stack trace of this exception at which the line of test code that failed resides.
 * @param undecoratedMessage just a short message that has no redundancy with args, labels, etc. The regular "message" has everything in it.
 * @param args the argument values, if any, that caused the property check to fail.
 * @param namesOfArgs an optional list of string names for the arguments.
 * @param labels the labels, if any (see the ScalaCheck user guide for information on labels)
 *
 * @throws NullPointerException if any parameter is <code>null</code> or <code>Some(null)</code>.
 *
 * @author Bill Venners
 */
class GeneratorDrivenPropertyCheckFailedException(
  messageFun: StackDepthException => String,
  cause: Option[Throwable],
  failedCodeStackDepthFun: StackDepthException => Int,
  undecoratedMessage: String,
  args: List[Any],
  namesOfArgs: Option[List[String]],
  val labels: List[String]
) extends PropertyCheckFailedException(messageFun, cause, failedCodeStackDepthFun, undecoratedMessage, args, namesOfArgs)

