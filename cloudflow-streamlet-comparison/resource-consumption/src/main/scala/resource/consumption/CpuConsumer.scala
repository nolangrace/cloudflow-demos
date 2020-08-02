package resource.consumption

object CpuConsumer {

  def calculatePrimes() = {
    (1 to 10000).foreach(isPrime(_))
  }

  def isPrime(i: Int): Boolean =
    if (i <= 1)
      false
    else if (i == 2)
      true
    else
      !(2 until i).exists(n => i % n == 0)

}
