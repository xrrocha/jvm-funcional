package registros

class TransRed[E, S, O](items: => Iterator[E],
                        transformar: (E) => S,
                        reducir: (Iterator[S]) => O)
  extends (() => O) :

  def apply(): O = reducir(items.map(transformar))



