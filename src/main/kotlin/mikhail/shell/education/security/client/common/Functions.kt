package mikhail.shell.education.security.client.common

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

fun Point.compose(other: Point, modNumber: BigInteger): Point {
    val lambda1 = (this.x * other.z.pow(2)).mod(modNumber)
    val lambda2 = (other.x * this.z.pow(2)).mod(modNumber)
    val lambda3 = (lambda2 - lambda1).mod(modNumber)
    val lambda4 = (this.y * other.z.pow(3)).mod(modNumber)
    val lambda5 = (other.y * this.z.pow(3)).mod(modNumber)
    val lambda6 = (lambda5 - lambda4).mod(modNumber)
    val lambda7 = (lambda1 + lambda2).mod(modNumber)
    val lambda8 = (lambda4 + lambda5).mod(modNumber)
    val Z3 = (this.z * other.z * lambda3).mod(modNumber)
    val X3 = (lambda6.pow(2) - lambda7 * lambda3.pow(2)).mod(modNumber)
    val lambda9 = (lambda7 * lambda3.pow(2) - BigInteger.TWO * X3).mod(modNumber)
    val Y3 = ((lambda9 * lambda6 - lambda8 * lambda3.pow(3)) * BigInteger.TWO.modInverse(modNumber)).mod(modNumber)
    return Point(X3, Y3, Z3)
}

fun Point.double(modNumber: BigInteger, a: BigInteger): Point {
    val lambda1 = (BigInteger("3") * this.x.pow(2) + a * this.z.pow(4)).mod(modNumber)
    val lambda2 = (BigInteger("4") * this.x * this.y.pow(2)).mod(modNumber)
    val Z3 = (BigInteger.TWO * this.y * this.z).mod(modNumber)
    val X3 = (lambda1.pow(2) - BigInteger.TWO * lambda2).mod(modNumber)
    val lambda3 = (BigInteger("8") * this.y.pow(4)).mod(modNumber)
    val Y3 = (lambda1 * (lambda2 - X3) - lambda3).mod(modNumber)
    return Point(X3, Y3, Z3)
}

fun Point.multiply(n: BigInteger, modNumber: BigInteger, a: BigInteger): Point {
    var result = Point(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO)
    var addend = this
    var exp = n
    while (exp > BigInteger.ZERO) {
        if (exp.testBit(0)) {
            result = result.compose(addend, modNumber)
        }
        addend = addend.double(modNumber, a)
        exp = exp shr 1
    }
    return result
}