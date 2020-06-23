/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.sequences

//
// NOTE: THIS FILE IS AUTO-GENERATED by the GenerateStandardLibTests.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

import test.comparisons.STRING_CASE_INSENSITIVE_ORDER
import test.collections.assertSorted
import kotlin.test.*

class _SequencesTest {
    @Test
    fun foldIndexed_Sequence() {
        expect(8) { sequenceOf<Int>(1, 2, 3).foldIndexed(0) { i, acc, e -> acc + i.toInt() * e } }
        expect(10) { sequenceOf<Int>(1, 2, 3).foldIndexed(1) { i, acc, e -> acc + i + e.toInt() } }
        expect(15) { sequenceOf<Int>(1, 2, 3).foldIndexed(1) { i, acc, e -> acc * (i.toInt() + e) } }
        expect(" 0-1 1-2 2-3") { sequenceOf<Int>(1, 2, 3).foldIndexed("") { i, acc, e -> "$acc $i-$e" } }
        expect(42) {
            val numbers = sequenceOf<Int>(1, 2, 3, 4)
            numbers.foldIndexed(0) { index, a, b -> index.toInt() * (a + b) }
        }
        expect(0) {
            val numbers = sequenceOf<Int>()
            numbers.foldIndexed(0) { index, a, b -> index.toInt() * (a + b) }
        }
        expect("11234") {
            val numbers = sequenceOf<Int>(1, 2, 3, 4)
            numbers.map { it.toString() }.foldIndexed("") { index, a, b -> if (index == 0) a + b + b else a + b }
        }
    }

    @Test
    fun maxByOrNull_Sequence() {
        assertEquals(null, sequenceOf<Int>().maxByOrNull { it })
        assertEquals(1, sequenceOf<Int>(1).maxByOrNull { it })
        assertEquals(3, sequenceOf<Int>(3, 2).maxByOrNull { it * it })
        assertEquals(3, sequenceOf<Int>(3, 2).maxByOrNull { "a" })
        assertEquals(3, sequenceOf<Int>(3, 2).maxByOrNull { it.toString() })
        assertEquals(2, sequenceOf<Int>(2, 3).maxByOrNull { -it })
        assertEquals('b', sequenceOf('a', 'b').maxByOrNull { "x$it" })
        assertEquals("abc", sequenceOf("b", "abc").maxByOrNull { it.length })
    }

    @Test
    fun maxWithOrNull_Sequence() {
        assertEquals(null, sequenceOf<Int>().maxWithOrNull(naturalOrder()))
        assertEquals(1, sequenceOf<Int>(1).maxWithOrNull(naturalOrder()))
        assertEquals(3, sequenceOf<Int>(2, 3, 4).maxWithOrNull(compareBy { it % 4 }))
        assertEquals("B", sequenceOf("a", "B").maxWithOrNull(STRING_CASE_INSENSITIVE_ORDER))
    }

    @Test
    fun minByOrNull_Sequence() {
        assertEquals(null, sequenceOf<Int>().minByOrNull { it })
        assertEquals(1, sequenceOf<Int>(1).minByOrNull { it })
        assertEquals(2, sequenceOf<Int>(3, 2).minByOrNull { it * it })
        assertEquals(3, sequenceOf<Int>(3, 2).minByOrNull { "a" })
        assertEquals(2, sequenceOf<Int>(3, 2).minByOrNull { it.toString() })
        assertEquals(3, sequenceOf<Int>(2, 3).minByOrNull { -it })
        assertEquals('a', sequenceOf('a', 'b').minByOrNull { "x$it" })
        assertEquals("b", sequenceOf("b", "abc").minByOrNull { it.length })
    }

    @Test
    fun minWithOrNull_Sequence() {
        assertEquals(null, sequenceOf<Int>().minWithOrNull(naturalOrder()))
        assertEquals(1, sequenceOf<Int>(1).minWithOrNull(naturalOrder()))
        assertEquals(4, sequenceOf<Int>(2, 3, 4).minWithOrNull(compareBy { it % 4 }))
        assertEquals("a", sequenceOf("a", "B").minWithOrNull(STRING_CASE_INSENSITIVE_ORDER))
    }

    @Test
    fun indexOf_Sequence() {
        expect(-1) { sequenceOf<Int>(1, 2, 3).indexOf(0) }
        expect(0) { sequenceOf<Int>(1, 2, 3).indexOf(1) }
        expect(1) { sequenceOf<Int>(1, 2, 3).indexOf(2) }
        expect(2) { sequenceOf<Int>(1, 2, 3).indexOf(3) } 
        expect(-1) { sequenceOf("cat", "dog", "bird").indexOf("mouse") }
        expect(0) { sequenceOf("cat", "dog", "bird").indexOf("cat") }
        expect(1) { sequenceOf("cat", "dog", "bird").indexOf("dog") }
        expect(2) { sequenceOf("cat", "dog", "bird").indexOf("bird") }
        expect(0) { sequenceOf(null, "dog", null).indexOf(null as String?)}
    }

    @Test
    fun indexOfFirst_Sequence() {
        expect(-1) { sequenceOf<Int>(1, 2, 3).indexOfFirst { it == 0 } }
        expect(0) { sequenceOf<Int>(1, 2, 3).indexOfFirst { it % 2 == 1 } }
        expect(1) { sequenceOf<Int>(1, 2, 3).indexOfFirst { it % 2 == 0 } }
        expect(2) { sequenceOf<Int>(1, 2, 3).indexOfFirst { it == 3 } }
        expect(-1) { sequenceOf("cat", "dog", "bird").indexOfFirst { it.contains("p") } }
        expect(0) { sequenceOf("cat", "dog", "bird").indexOfFirst { it.startsWith('c') } }
        expect(1) { sequenceOf("cat", "dog", "bird").indexOfFirst { it.startsWith('d') } }
        expect(2) { sequenceOf("cat", "dog", "bird").indexOfFirst { it.endsWith('d') } }
    }

    @Test
    fun sorted_Sequence() {
        sequenceOf<Int>(3, 7, 1).sorted().iterator().assertSorted { a, b -> a.compareTo(b) <= 0 }
        sequenceOf(1, Int.MAX_VALUE, Int.MIN_VALUE).sorted().iterator().assertSorted { a, b -> a.compareTo(b) <= 0 }
        sequenceOf("ac", "aD", "aba").sorted().iterator().assertSorted { a, b -> a.compareTo(b) <= 0 }
    }

    @Test
    fun sortedDescending_Sequence() {
        sequenceOf<Int>(3, 7, 1).sortedDescending().iterator().assertSorted { a, b -> a.compareTo(b) >= 0 }
        sequenceOf(1, Int.MAX_VALUE, Int.MIN_VALUE).sortedDescending().iterator().assertSorted { a, b -> a.compareTo(b) >= 0 }
        sequenceOf("ac", "aD", "aba").sortedDescending().iterator().assertSorted { a, b -> a.compareTo(b) >= 0 }
    }

    @Test
    fun sortedWith_Sequence() {
        val comparator = compareBy { it: Int -> it % 3 }.thenByDescending { it }
        sequenceOf<Int>(0, 1, 2, 3, 4, 5).sortedWith(comparator).iterator().assertSorted { a, b -> comparator.compare(a, b) <= 0 }
    }

}
