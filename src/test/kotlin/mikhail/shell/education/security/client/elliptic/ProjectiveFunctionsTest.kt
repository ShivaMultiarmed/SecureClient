package mikhail.shell.education.security.client.elliptic

import mikhail.shell.education.security.client.common.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ProjectiveFunctionsTest {
    @Test
    fun testProjectiveComposing() {
        val a = BigInteger("5") to BigInteger.ONE
        val b = BigInteger("4") to BigInteger("6")
        val A = a.toPoint()
        val B = b.toPoint()
        val expected = BigInteger("2") to BigInteger("5")
        val actual = A.compose(B, BigInteger("7")).toPair()
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun testDoublingProjectives() {
        val a = BigInteger("5") to BigInteger.ONE
        val expected = BigInteger("4") to BigInteger("6")
        val A = a.toPoint()
        val R = A.double(BigInteger("7"), BigInteger("2"))
        val actual = R.toPair()
        Assertions.assertEquals(expected, actual)
    }
}