// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// IGNORE_BACKEND: JVM, JVM_IR

// expected: rv: abc

// KT-19423 variation
val used = "abc"

class Outer {
    class User {
        val property = used
    }
}

val rv = Outer.User().property
