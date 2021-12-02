interface A<T> {
    fun id(t: T): T
}

open class B : A<String> {
    override fun id(t: String) = t
}

class C : B()

class D : A<String> by C()
