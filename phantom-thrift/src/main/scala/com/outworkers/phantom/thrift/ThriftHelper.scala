/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.thrift

import com.twitter.scrooge.ThriftStructSerializer

import scala.reflect.macros.blackbox

trait ThriftHelper[
  ValueType <: ThriftStruct,
  Serializer[X] <: ThriftStructSerializer[X]
] {
  def serializer: Serializer[ValueType]
}

object ThriftHelper {
  def apply[
    T <: ThriftStruct,
    Serializer[X] <: ThriftStructSerializer[X]
  ](implicit ev: ThriftHelper[T, Serializer]): ThriftHelper[T, Serializer] = ev

  implicit def materializer[
    T <: ThriftStruct,
    Serializer[X] <: ThriftStructSerializer[X]
  ]: ThriftHelper[T, Serializer] = macro ThriftHelperMacro.materialize[T, Serializer]
}

@macrocompat.bundle
class ThriftHelperMacro(val c: blackbox.Context) {

  import c.universe._

  private[this] val pkgRoot = q"_root_.com.outworkers.phantom.thrift"
  private[this] val scroogePkg = q"_root_.com.twitter.scrooge"
  private[this] val serializerTpe: (Type, Type) => Tree = {
    case (sTpe, vTpe) => tq"$sTpe[$vTpe]"
  }

  def materialize[
    T <: ThriftStruct : WeakTypeTag,
    Serializer[_] : WeakTypeTag
  ]: Tree = {
    val tpe = weakTypeOf[T]
    val sTpe = weakTypeOf[Serializer[_]]
    //sTpe.typeConstructor
    val valueCompanion = tpe.typeSymbol.companion
    val serializerCompanion = sTpe.typeSymbol.companion

    q"""
      new $pkgRoot.ThriftHelper[$tpe, $sTpe] {
        override val serializer: ${serializerTpe(sTpe, tpe)} = {
          $scroogePkg.$serializerCompanion.apply[$tpe]($valueCompanion)
        }
      }
    """
  }

}