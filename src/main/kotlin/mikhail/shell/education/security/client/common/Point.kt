package mikhail.shell.education.security.client.common

import java.math.BigInteger

data class Point(
    val x: BigInteger,
    val y: BigInteger,
    val z: BigInteger
)

fun Pair<BigInteger, BigInteger>.toPoint(): Point {
    return Point(this.first, this.second, BigInteger.ONE)
}

fun Point.toPair(): Pair<BigInteger, BigInteger> {
    return this.x to this.y
}