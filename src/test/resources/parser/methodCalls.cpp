struct Foo {
	void instanceMethod();
	static void staticMethod();
	virtual void abstractMethod() = 0;
};

void instanceMethodCall(Foo *p) {
	p->instanceMethod();
}

void staticMethodCall() {
	Foo::staticMethod();
}

void abstractMethodCall(Foo *p) {
	p->abstractMethod();
}

struct Bar : public Foo {
    void abstractMethod();
};

extern Bar bar;
void testBar() {
    abstractMethodCall(&bar);
}