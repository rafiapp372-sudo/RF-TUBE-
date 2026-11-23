package com.example.data

data class SpamCheckResult(
    val isSpam: Boolean,
    val reasonEn: String = "",
    val reasonBn: String = ""
)

object SpamDetector {
    private val suspiciousPhrases = listOf(
        "free money",
        "win iphone",
        "click here now",
        "crypto giveaway",
        "double your bitcoin",
        "guaranteed profit",
        "ফ্রি টাকা",
        "পুরস্কার জিতুন"
    )

    private var lastPublishEpochMs = 0L

    fun inspect(text: String, enforceRateLimit: Boolean = false): SpamCheckResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SpamCheckResult(false)

        val lower = trimmed.lowercase()
        val matchedKeyword = suspiciousPhrases.firstOrNull { lower.contains(it) }
        if (matchedKeyword != null) {
            return SpamCheckResult(
                isSpam = true,
                reasonEn = "Blocked by spam filter: promotional scam phrase detected (\"$matchedKeyword\").",
                reasonBn = "স্প্যাম ফিল্টার দ্বারা বাতিল হয়েছে: সন্দেহজনক প্রচারণামূলক শব্দ শনাক্ত হয়েছে (\"$matchedKeyword\")।"
            )
        }

        val urlCount = Regex("https?://\\S+").findAll(lower).count()
        if (urlCount >= 2) {
            return SpamCheckResult(
                isSpam = true,
                reasonEn = "Blocked by spam filter: multiple external links are not allowed in a single entry.",
                reasonBn = "স্প্যাম ফিল্টার দ্বারা বাতিল হয়েছে: একসাথে একাধিক বাহ্যিক লিংক অনুমোদিত নয়।"
            )
        }

        if (Regex("(.)\\1{6,}").containsMatchIn(trimmed)) {
            return SpamCheckResult(
                isSpam = true,
                reasonEn = "Blocked by spam filter: excessive repeated characters detected.",
                reasonBn = "স্প্যাম ফিল্টার দ্বারা বাতিল হয়েছে: অতিরিক্ত পুনরাবৃত্তি অক্ষর শনাক্ত হয়েছে।"
            )
        }

        val letters = trimmed.filter { it.isLetter() }
        if (letters.length >= 16 && letters.count { it.isUpperCase() } > (letters.length * 0.85f)) {
            return SpamCheckResult(
                isSpam = true,
                reasonEn = "Blocked by spam filter: excessive ALL-CAPS shouting detected.",
                reasonBn = "স্প্যাম ফিল্টার দ্বারা বাতিল হয়েছে: অতিরিক্ত বড় হাতের অক্ষর ব্যবহার করা হয়েছে।"
            )
        }

        if (enforceRateLimit) {
            val now = System.currentTimeMillis()
            if (now - lastPublishEpochMs < 2000L) {
                return SpamCheckResult(
                    isSpam = true,
                    reasonEn = "Slow down: please wait a couple seconds before posting again.",
                    reasonBn = "অনুগ্রহ করে পুনরায় পোস্ট করার আগে কয়েক সেকেন্ড অপেক্ষা করুন।"
                )
            }
            lastPublishEpochMs = now
        }

        return SpamCheckResult(false)
    }
}
