package mikhail.shell.education.security.client

import java.math.BigInteger
import java.security.SecureRandom

val random = SecureRandom()

fun generateSecretKey(q: BigInteger): BigInteger {
    var secret: BigInteger
    do {
        secret = BigInteger(q.bitLength(), random)
    } while (secret < BigInteger.ONE || secret >= q || !secret.isProbablePrime(100))
    return secret
}

// Генерирует простое число длины bits
fun generatePrime(bits: Int): BigInteger {
    return BigInteger(bits, 100, random)
}

// Генерирует 1024-bit простое число p = kq + 1
fun generateSafePrime(q: BigInteger): BigInteger {
    var p: BigInteger
    do {
        // Гарантирует единицу в самом старшем бите, а следовательно число становится достаточно большим.
        val k = BigInteger(768, random).setBit(767)
        p = k.multiply(q).add(BigInteger.ONE)
    } while (!p.isProbablePrime(100))
    return p
}

// Ищет генератор для подгруппы порядка q
fun findGenerator(p: BigInteger, q: BigInteger): BigInteger {
    // t = (p-1)/q
    val t = (p - BigInteger.ONE) / q
    val maxAttempts = 1_000_000
    for (i in 0 until maxAttempts) {
        // Выбираем случайное число r в диапазоне [2, p-2].
        val r = (BigInteger(p.bitLength(), random) % (p - BigInteger.valueOf(3))) + BigInteger.TWO
        // Вычисляем g = r^t mod p. Тогда g принадлежит подгруппе порядка q, поскольку g^q = r^(t*q) = r^(p-1) ≡ 1 mod p.
        val g = r.modPow(t, p)
        // Если g не равно 1, то оно является нетривиальным элементом подгруппы (а в циклической подгруппе порядка q любой нетривиальный элемент является её генератором).
        if (g != BigInteger.ONE) {
            return g
        }
    }
    throw RuntimeException("Не удалось найти генератор циклической подгруппы порядка q за $maxAttempts попыток")
}

fun Pair<BigInteger, BigInteger>.evaluateCompositionByTwo(modNumber: BigInteger, a: BigInteger): Pair<BigInteger, BigInteger> {
    return this.evaluateComposition(this, modNumber, a)
}

fun Pair<BigInteger, BigInteger>.evaluateComposition(
    other: Pair<BigInteger, BigInteger>,
    modNumber: BigInteger,
    a: BigInteger? = null
):Pair<BigInteger, BigInteger> {
    val (x1, y1) = this
    if (this == other) {
        val numerator = (BigInteger("3") * x1.modPow(BigInteger.TWO, modNumber) + a!!).mod(modNumber)
        val denominator = (BigInteger.TWO * y1).mod(modNumber)
        val denominatorInversed = denominator.modInverse(modNumber)
        val k = (numerator * denominatorInversed).mod(modNumber)
        val x3 = (k.modPow(BigInteger.TWO, modNumber) - BigInteger.TWO * x1).mod(modNumber)
        val y3 = (k * (x1 - x3) - y1).mod(modNumber)
        return x3 to y3
    } else {
        val (x2, y2) = other
        val numerator = (y2 - y1).mod(modNumber)
        val denominator = (x2 - x1).mod(modNumber)
        val denominatorInversed = denominator.modInverse(modNumber)
        val k = (numerator * denominatorInversed).mod(modNumber)
        val x3 = (k.pow(2) - x1 - x2).mod(modNumber)
        val y3 = (k * (x1 - x3) - y1).mod(modNumber)
        return x3 to y3
    }
}

fun Pair<BigInteger, BigInteger>.evaluateComposition(n: BigInteger, modNumber: BigInteger, a: BigInteger): Pair<BigInteger, BigInteger> {
    var result: Pair<BigInteger, BigInteger>? = null
    var addend: Pair<BigInteger, BigInteger>? = this

    var k = n
    while (k > BigInteger.ZERO) {
        if (k.testBit(0)) {
            result = if (result == null) addend
            else result.evaluateComposition(addend!!, modNumber)
        }
        addend = addend?.evaluateCompositionByTwo(modNumber, a)
        k = k.shiftRight(1)
    }
    return result ?: (BigInteger.ZERO to BigInteger.ZERO)
}

fun BigInteger.getBit(n: BigInteger): BigInteger {
    return this shr n.toInt() and BigInteger.ONE
}