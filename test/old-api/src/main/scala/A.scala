package p

class A {
  // keep
  def f1 = 1

  // deprecated removed 
  @deprecated
  def f2 = 2

  // removed
  def f3 = 3

  @deprecated
  type C[T] = List[T]
}

// deprecate all underlying symbols
@deprecated
class B {
  def f1 = 1
  def f2 = 2
}