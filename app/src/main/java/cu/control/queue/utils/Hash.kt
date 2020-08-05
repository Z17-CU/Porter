package cu.control.queue.utils

import java.security.MessageDigest

class Hash {

    companion object {

        fun getSha1(message: String, secure: String = ""): String {

            val bytes =
                MessageDigest.getInstance("SHA-1").digest("${message}:${secure}".toByteArray())
            return bytes.joinToString("") {
                "%02x".format(it)
            }

        }

        fun getMd5(message: String, secure: String = ""): String {

            val bytes =
                MessageDigest.getInstance("MD5").digest("${message}:${secure}".toByteArray())
            return bytes.joinToString("") {
                "%02x".format(it)
            }

        }

        /**
         * Convertir el hash MD5 del mensaje a un numero Long
         */
        fun getLongHash(message: String, secure: String = ""): Long {

            var longString = ""
            val md5 = getMd5(message, secure)

            for (c in md5) {

                longString += try {
                    "$c".toInt()
                } catch (e: NumberFormatException) {
                    c.toInt()
                }

            }

            val maxLength = Long.MAX_VALUE.toString().length
            return longString.substring(0 until maxLength).toLong()
        }

    }
}