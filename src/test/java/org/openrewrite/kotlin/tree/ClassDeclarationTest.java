/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.kotlin.tree;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.openrewrite.Issue;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.kotlin.tree.ParserAssertions.kotlin;

class ClassDeclarationTest implements RewriteTest {

    @Test
    void multipleClassDeclarationsInOneCompilationUnit() {
        rewriteRun(
          kotlin(
            """
            package some.package.name
            class A { }
            class B { }
            """
          )
        );
    }

    @Test
    void empty() {
        rewriteRun(
          kotlin(
            """
            class A
            class B
            """
          )
        );
    }

    @Test
    void classImplements() {
        rewriteRun(
          kotlin(
            """
            interface A
            interface B
            class C : A , B
            class D : B , A
            """
          )
        );
    }

    @Test
    void classExtends() {
        rewriteRun(
          kotlin(
            """
            open class A
            class B : A ( )
            """
          )
        );
    }

    @Test
    void extendsAndImplementsInMixedOrder() {
        rewriteRun(
          kotlin(
            """
            interface A
            interface B
            open class C

            class D : A , C ( ) , B
            """
          )
        );
    }

    @Test
    void innerClass() {
        rewriteRun(
          kotlin(
            """
              interface C {
                  class Inner {
                  }
              }
              """
          )
        );
    }

    @Test
    void modifierOrdering() {
        rewriteRun(
          kotlin("public /* comment */ abstract open class A")
        );
    }

    @Test
    void annotationClass() {
        rewriteRun(
          kotlin("annotation class A")
        );
    }

    @Test
    void enumClass() {
        rewriteRun(
          kotlin("enum class A")
        );
    }

    @Test
    void annotation() {
        rewriteRun(
          kotlin(
            """
            public @Deprecated ( "message 0" ) abstract @Suppress("") class Test
            
            @Deprecated ( "message 1" )
            @Suppress ( "" )
            class A
            
            @Suppress ( "unused" , "unchecked" )
            @Deprecated ( "message 2" )
            class B
            """
          )
        );
    }

    @Test
    void quotedIdentifier() {
        rewriteRun(
          kotlin("class `Quoted id here`")
        );
    }

    @Test
    void typeArguments() {
        rewriteRun(
          kotlin("open class B < T > { }")
        );
    }

    @Test
    void singleBoundedTypeParameters() {
        rewriteRun(
          kotlin(
            """
            interface A
            interface B
            
            class KotlinTypeGoat < T : A , S : B>
            """
          )
        );
    }

    @Test
    void primaryConstructor() {
        rewriteRun(
          kotlin("class Test ( val answer : Int )")
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/74")
    @Test
    void secondaryConstructor() {
        rewriteRun(
          kotlin(
            """
            class Test ( val answer : Int ) {
                constructor ( ) : this ( 42 )
            }
            """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/94")
    @Test
    void explicitInlineConstructor() {
        rewriteRun(
          kotlin("class Test internal constructor ()")
        );
    }

    @Test
    void implicitConstructorWithSuperType() {
        rewriteRun(
          kotlin("class Other"),
          kotlin("class Test ( val answer : Int ) : Other ( ) { }")
        );
    }

    @Test
    void singleLineCommentBeforeModifier() {
        rewriteRun(
          kotlin(
            """
            @Deprecated ( "" )
            // Some comment
            open class A
            """
          )
        );
    }

    @ExpectedToFail
    @Test
    void multipleBounds() {
        rewriteRun(
          kotlin(
            """
            interface A
            interface B
            interface C
            interface D
            
            class KotlinTypeGoat < T , S > where S : A , T : D , S : B , T : C
            """
          )
        );
    }

    @Test
    void object() {
        rewriteRun(
          kotlin(" object Test")
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/32")
    @Test
    void companionObject() {
        rewriteRun(
          kotlin("object Companion"),
          kotlin(
            """
            class Test {
                companion object
            }
            """
          )
        );
    }

    @Test
    void variance() {
        rewriteRun(
          kotlin("interface A < in R >"),
          kotlin("interface B < out R >")
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/72")
    @Test
    void sealedClassWithPropertiesAndDataClass() {
        rewriteRun(
          kotlin(
            """
            sealed class InvalidField {
              abstract val field: String
            }
            data class InvalidEmail( val errors : List<String> ) : InvalidField ( ) {
              override val field : String = "email"
            }
            """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/72")
    @Test
    void sealedInterfaceWithPropertiesAndDataClass() {
        rewriteRun(
          kotlin(
            """
            sealed interface InvalidField {
              val field : String
            }
            data class InvalidEmail( val errors : List<String> ) : InvalidField {
              override val field : String = "email"
            }
            """
          )
        );
    }

    @Test
    void sealedInterfaceWithPropertiesAndObject() {
        rewriteRun(
          kotlin(
            """
            sealed interface InvalidField {
              val field : String
            }
            object InvalidEmail : InvalidField {
              override val field : String = "email"
            }
            """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/68")
    @Test
    void init() {
        rewriteRun(
          kotlin("""
            class Test {
                init {
                    println( "Hello, world!" )
                }
            }
            """)
        );
    }

    @Test
    void valueClass() {
        rewriteRun(
          kotlin("@JvmInline value class Wrapper ( val int : Int )")
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/66")
    @Test
    void typeParameterReference() {
        rewriteRun(
          kotlin(
            """
            abstract class BaseSubProjectionNode < T , R > (
                val parent : T,
                val root : R
            ) {
            
                constructor ( parent : T , root : R ) : this ( parent , root )
            
                fun parent ( ) : T {
                    return parent
                }
            
                fun root ( ) : R {
                    return root
                }
            }
            """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite-kotlin/issues/99")
    @Test
    void implicitThis() {
        rewriteRun(
          kotlin(
            """
            abstract class Test(arg: Test.() -> Unit = {}) {
                init {
                    arg()
                }
            }
            """
          )
        );
    }
}
