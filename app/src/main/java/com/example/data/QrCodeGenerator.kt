package com.example.data

import java.nio.charset.StandardCharsets

/**
 * Pure-Kotlin QR Code generator (Version 3, 29x29, Error Correction Level L, Byte Mode).
 * Produces a valid scannable QR matrix for channel URLs up to 53 UTF-8 bytes without extra dependencies.
 */
object QrCodeGenerator {
    private const val SIZE = 29 // Version 3 = 17 + 4*3
    private const val TOTAL_CODEWORDS = 70
    private const val DATA_CODEWORDS = 55
    private const val ECC_CODEWORDS = 15

    private val GF_EXP = IntArray(512)
    private val GF_LOG = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            GF_EXP[i] = x
            GF_LOG[x] = i
            x = x shl 1
            if (x and 0x100 != 0) x = x xor 0x11D
        }
        for (i in 255 until 512) {
            GF_EXP[i] = GF_EXP[i - 255]
        }
    }

    fun generate(payload: String): Array<BooleanArray> {
        val bytes = payload.toByteArray(StandardCharsets.UTF_8).take(53).toByteArray()
        val data = encodeDataCodewords(bytes)
        val ecc = computeReedSolomon(data, ECC_CODEWORDS)
        val allBytes = data + ecc

        val modules = Array(SIZE) { BooleanArray(SIZE) }
        val isFunction = Array(SIZE) { BooleanArray(SIZE) }

        drawFinderPattern(modules, isFunction, 0, 0)
        drawFinderPattern(modules, isFunction, SIZE - 7, 0)
        drawFinderPattern(modules, isFunction, 0, SIZE - 7)
        drawAlignmentPattern(modules, isFunction, 20, 20)
        drawTimingPatterns(modules, isFunction)

        // Dark module
        modules[SIZE - 8][8] = true
        isFunction[SIZE - 8][8] = true

        // Reserve format info bits
        for (i in 0..8) {
            if (i != 6) {
                isFunction[8][i] = true
                isFunction[i][8] = true
            }
        }
        for (i in SIZE - 8 until SIZE) {
            isFunction[8][i] = true
            isFunction[i][8] = true
        }

        placeDataBits(modules, isFunction, allBytes)
        applyMask(modules, isFunction, mask = 0)
        drawFormatBits(modules, eclBits = 1, mask = 0) // ECL L = 01
        return modules
    }

    private fun encodeDataCodewords(payload: ByteArray): IntArray {
        val bits = ArrayList<Int>(DATA_CODEWORDS * 8)
        fun pushBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                bits.add((value ushr i) and 1)
            }
        }
        // Byte mode indicator 0100 + 8-bit length
        pushBits(0b0100, 4)
        pushBits(payload.size, 8)
        for (b in payload) {
            pushBits(b.toInt() and 0xFF, 8)
        }
        // Terminator
        val remaining = DATA_CODEWORDS * 8 - bits.size
        pushBits(0, minOf(4, remaining))
        while (bits.size % 8 != 0) bits.add(0)

        val out = IntArray(DATA_CODEWORDS)
        var idx = 0
        for (i in 0 until bits.size step 8) {
            var v = 0
            for (j in 0 until 8) v = (v shl 1) or bits[i + j]
            out[idx++] = v
        }
        var pad = 0
        while (idx < DATA_CODEWORDS) {
            out[idx++] = if (pad % 2 == 0) 0xEC else 0x11
            pad++
        }
        return out
    }

    private fun gfMul(a: Int, b: Int): Int =
        if (a == 0 || b == 0) 0 else GF_EXP[GF_LOG[a] + GF_LOG[b]]

    private fun computeReedSolomon(data: IntArray, eccLen: Int): IntArray {
        var gen = intArrayOf(1)
        for (i in 0 until eccLen) {
            val next = IntArray(gen.size + 1)
            val factor = GF_EXP[i]
            for (j in gen.indices) {
                next[j] = next[j] xor gen[j]
                next[j + 1] = next[j + 1] xor gfMul(gen[j], factor)
            }
            gen = next
        }
        val res = IntArray(eccLen)
        for (b in data) {
            val factor = b xor res[0]
            System.arraycopy(res, 1, res, 0, eccLen - 1)
            res[eccLen - 1] = 0
            for (j in 0 until eccLen) {
                res[j] = res[j] xor gfMul(gen[j + 1], factor)
            }
        }
        return res
    }

    private fun drawFinderPattern(m: Array<BooleanArray>, f: Array<BooleanArray>, row: Int, col: Int) {
        for (dr in -1..7) {
            for (dc in -1..7) {
                val r = row + dr
                val c = col + dc
                if (r in 0 until SIZE && c in 0 until SIZE) {
                    val dark = dr in 0..6 && dc in 0..6 &&
                        (dr == 0 || dr == 6 || dc == 0 || dc == 6 || (dr in 2..4 && dc in 2..4))
                    m[r][c] = dark
                    f[r][c] = true
                }
            }
        }
    }

    private fun drawAlignmentPattern(m: Array<BooleanArray>, f: Array<BooleanArray>, row: Int, col: Int) {
        for (dr in -2..2) {
            for (dc in -2..2) {
                val r = row + dr
                val c = col + dc
                m[r][c] = kotlin.math.max(kotlin.math.abs(dr), kotlin.math.abs(dc)) != 1
                f[r][c] = true
            }
        }
    }

    private fun drawTimingPatterns(m: Array<BooleanArray>, f: Array<BooleanArray>) {
        for (i in 8 until SIZE - 8) {
            val dark = i % 2 == 0
            if (!f[6][i]) {
                m[6][i] = dark
                f[6][i] = true
            }
            if (!f[i][6]) {
                m[i][6] = dark
                f[i][6] = true
            }
        }
    }

    private fun placeDataBits(m: Array<BooleanArray>, f: Array<BooleanArray>, allBytes: IntArray) {
        var bitIndex = 0
        val totalBits = allBytes.size * 8
        var upward = true
        var col = SIZE - 1
        while (col > 0) {
            if (col == 6) col--
            for (i in 0 until SIZE) {
                val row = if (upward) SIZE - 1 - i else i
                for (dc in 0..1) {
                    val c = col - dc
                    if (!f[row][c]) {
                        val bit = if (bitIndex < totalBits) {
                            ((allBytes[bitIndex / 8] ushr (7 - (bitIndex % 8))) and 1) == 1
                        } else false
                        m[row][c] = bit
                        bitIndex++
                    }
                }
            }
            upward = !upward
            col -= 2
        }
    }

    private fun applyMask(m: Array<BooleanArray>, f: Array<BooleanArray>, mask: Int) {
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (!f[r][c] && ((r + c) % 2 == 0) && mask == 0) {
                    m[r][c] = !m[r][c]
                }
            }
        }
    }

    private fun drawFormatBits(m: Array<BooleanArray>, eclBits: Int, mask: Int) {
        val data = (eclBits shl 3) or mask
        var rem = data
        for (i in 0 until 10) rem = (rem shl 1) xor ((rem ushr 9) * 0x537)
        val bits = ((data shl 10) or rem) xor 0x5412

        for (i in 0..5) m[8][i] = ((bits ushr i) and 1) == 1
        m[8][7] = ((bits ushr 6) and 1) == 1
        m[8][8] = ((bits ushr 7) and 1) == 1
        m[7][8] = ((bits ushr 8) and 1) == 1
        for (i in 9 until 15) m[14 - i][8] = ((bits ushr i) and 1) == 1

        for (i in 0..7) m[SIZE - 1 - i][8] = ((bits ushr i) and 1) == 1
        for (i in 8 until 15) m[8][SIZE - 15 + i] = ((bits ushr i) and 1) == 1
    }
}
