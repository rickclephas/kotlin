// WITH_RUNTIME

fun testCustomFunction() {
    inline fun doIt(f: () -> Int): Int = f()
    inline fun calcOnePlusTwo(f: (Int) -> Int): Int = f(1) + f(2)
    inline fun getFirstArg(a: Int, vararg other: Int): Int = a

    val x = doIt { calcOnePlusTwo(::getFirstArg) }
    assertEquals(x, 3)
}

fun testRuntimeFunctionCase1() {
    val x = "123".let { it.minOf(::maxOf) }
    assertEquals(x, '1')
}

fun testRuntimeFunctionCase2() {
    val x = "3123".minOfOrNull { a: Char -> a.titlecase().maxOf(::maxOf) }
    assertEquals(x, '1')
}

fun box(): String {
    testCustomFunction()
    testRuntimeFunctionCase1()
    testRuntimeFunctionCase2()
    return "OK"
}
