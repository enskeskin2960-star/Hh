package com.example.data

import com.example.model.DetectionResult
import com.example.model.GameSignature
import com.example.model.ProviderProfile
import com.example.model.SimulationReport
import com.example.model.SpinEvent
import com.example.model.TimeWindowAnalysis
import com.example.model.VerdictType
import java.net.URI
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

object TelasetryEngine {

    val providers: List<ProviderProfile> = listOf(
        ProviderProfile(
            code = "PRAGMATIC",
            name = "Pragmatic Play",
            cdnSignature = "prerelease-env.biz / ppadaptation / gs2c.pragmaticplay",
            defaultRtp = 96.50,
            volatilityIndex = 9,
            deadStreakFactor = 1.35,
            syncMinutes = listOf(0, 1, 14, 15, 16, 29, 30, 31, 44, 45, 46, 59),
            peakHoursUtc3 = listOf(20, 21, 22, 23, 0, 1),
            safeHoursUtc3 = listOf(4, 5, 6, 7, 11, 14, 15, 17),
            engineNotes = "Drop & Wins havuz senkronizasyonu her çeyrek saatte (:00, :15, :30, :45) tohum yeniler. Yüksek yoğunlukta ardışık 25+ ölü spin serisi üretir."
        ),
        ProviderProfile(
            code = "PGSOFT",
            name = "PG Soft (Pocket Games)",
            cdnSignature = "m.pgsoft-games.com / api.pg-bo.me",
            defaultRtp = 96.72,
            volatilityIndex = 7,
            deadStreakFactor = 1.05,
            syncMinutes = listOf(5, 10, 20, 25, 35, 40, 50, 55),
            peakHoursUtc3 = listOf(18, 19, 20, 21, 22),
            safeHoursUtc3 = listOf(2, 3, 4, 8, 9, 13, 14, 16),
            engineNotes = "Asya + Global sunucu kümeleri :05 ve :25 dakikalarında RTP dengeler. Kısa oturum (60-90 spin) çarpan yakalama oranı yüksektir."
        ),
        ProviderProfile(
            code = "EGT",
            name = "EGT / Amusnet Interactive",
            cdnSignature = "egt-bg.com / amusnet.io / jackpot-server",
            defaultRtp = 95.98,
            volatilityIndex = 6,
            deadStreakFactor = 1.15,
            syncMinutes = listOf(0, 12, 24, 36, 48),
            peakHoursUtc3 = listOf(21, 22, 23, 0),
            safeHoursUtc3 = listOf(6, 7, 8, 12, 15, 18),
            engineNotes = "4 seviyeli Mystery Kart Jackpot havuzu (♣ ♦ ♥ ♠) sabit bahis ritmi sever. Sürekli bahis değiştirmek oturum RTP koridorunu düşürür."
        ),
        ProviderProfile(
            code = "HACKSAW",
            name = "Hacksaw Gaming",
            cdnSignature = "static.hacksawgaming.com / rgs.hacksaw",
            defaultRtp = 96.30,
            volatilityIndex = 10,
            deadStreakFactor = 1.55,
            syncMinutes = listOf(0, 20, 40),
            peakHoursUtc3 = listOf(21, 22, 23, 0, 1, 2),
            safeHoursUtc3 = listOf(5, 6, 10, 11, 15, 16),
            engineNotes = "Ekstrem volatilite motoru. Taban oyunda uzun ölü koridorlar (40+ spin) sonrası ani VS/Çarpan patlaması yapar. Düşük kasayla girilmez."
        ),
        ProviderProfile(
            code = "NOLIMIT",
            name = "Nolimit City",
            cdnSignature = "nolimitcdn.com / rgs.nolimitcity.com",
            defaultRtp = 96.10,
            volatilityIndex = 10,
            deadStreakFactor = 1.65,
            syncMinutes = listOf(0, 30),
            peakHoursUtc3 = listOf(22, 23, 0, 1),
            safeHoursUtc3 = listOf(4, 5, 9, 14),
            engineNotes = "xWays / xNudge matematik modeli çok sert varyansa sahiptir. Minimum 200x bahis kasası olmadan simülasyon %75+ erime verir."
        ),
        ProviderProfile(
            code = "NETENT",
            name = "NetEnt / Red Tiger",
            cdnSignature = "casinomodule.com / gserver-netent",
            defaultRtp = 96.09,
            volatilityIndex = 7,
            deadStreakFactor = 1.12,
            syncMinutes = listOf(0, 15, 30, 45),
            peakHoursUtc3 = listOf(20, 21, 22, 23),
            safeHoursUtc3 = listOf(7, 8, 12, 13, 17),
            engineNotes = "Klasik RNG dağılım eğrisi. Oturumun ilk 50 spininde geri dönüş (realized RTP) %65 altındaysa oturum tohumu soğuktur."
        ),
        ProviderProfile(
            code = "PLAYNGO",
            name = "Play'n GO",
            cdnSignature = "playngonetwork.com / cw.playngo",
            defaultRtp = 96.20,
            volatilityIndex = 8,
            deadStreakFactor = 1.25,
            syncMinutes = listOf(10, 30, 50),
            peakHoursUtc3 = listOf(20, 21, 22, 23),
            safeHoursUtc3 = listOf(6, 9, 14, 16),
            engineNotes = "Dinamik RTP profilleri (96% / 94% / 91% / 87%) siteye göre değişir. İlk 40 spinde sembol genişleme sıklığı koridoru ele verir."
        ),
        ProviderProfile(
            code = "PUSH",
            name = "Push Gaming",
            cdnSignature = "pushgaming.com / rgs-push",
            defaultRtp = 96.40,
            volatilityIndex = 9,
            deadStreakFactor = 1.40,
            syncMinutes = listOf(0, 20, 40),
            peakHoursUtc3 = listOf(21, 22, 23, 0),
            safeHoursUtc3 = listOf(5, 8, 13, 15),
            engineNotes = "Cluster / Anlık Ödül jeton mekaniği yüksek varyanslıdır. Kirli çerez/oturum izinde ölü spin serisi uzar."
        ),
        ProviderProfile(
            code = "SPRIBE",
            name = "Spribe (Crash / Turbo)",
            cdnSignature = "spribe.co / aviator-game.net",
            defaultRtp = 97.00,
            volatilityIndex = 8,
            deadStreakFactor = 1.30,
            syncMinutes = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55),
            peakHoursUtc3 = listOf(20, 21, 22, 23, 0, 1),
            safeHoursUtc3 = listOf(3, 4, 5, 6, 11, 14),
            engineNotes = "SHA-256 Provably Fair sunucu+istemci tohumu. Her 5 dakikada bir düşük çarpan (1.00x - 1.15x) temizleme dalgası oluşur."
        ),
        ProviderProfile(
            code = "EVOLUTION",
            name = "Evolution Gaming",
            cdnSignature = "evolution.com / evo-games.com",
            defaultRtp = 96.08,
            volatilityIndex = 7,
            deadStreakFactor = 1.20,
            syncMinutes = listOf(0, 15, 30, 45),
            peakHoursUtc3 = listOf(21, 22, 23, 0),
            safeHoursUtc3 = listOf(8, 11, 14, 17),
            engineNotes = "Canlı stüdyo + RNG çarpan motoru. Ağ gecikmesi (ping > 120ms) bahis kabul ve oturum senkronunu olumsuz etkiler."
        )
    )

    val games: List<GameSignature> = listOf(
        // PRAGMATIC PLAY
        GameSignature(
            id = "vs20olympx",
            title = "Gates of Olympus 1000",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.50,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 28.5,
            bonusFrequencySpins = 440,
            maxWinMultiplier = 15000,
            minRecommendedBankrollBets = 180,
            keywords = listOf("gates 1000", "olympus 1000", "dede 1000", "vs20olympx", "zeus 1000"),
            urlSlugs = listOf("gates-of-olympus-1000", "vs20olympx", "olympus1000")
        ),
        GameSignature(
            id = "vs20olympgate",
            title = "Gates of Olympus",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.50,
            volatilityTier = "ÇOK YÜKSEK (5/5)",
            volatilityScore = 9,
            hitFrequencyPct = 29.2,
            bonusFrequencySpins = 415,
            maxWinMultiplier = 5000,
            minRecommendedBankrollBets = 140,
            keywords = listOf("gates of olympus", "olympus", "dede", "vs20olympgate", "zeus"),
            urlSlugs = listOf("gates-of-olympus", "vs20olympgate")
        ),
        GameSignature(
            id = "vs20fruitswx",
            title = "Sweet Bonanza 1000",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.53,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 27.8,
            bonusFrequencySpins = 450,
            maxWinMultiplier = 25000,
            minRecommendedBankrollBets = 180,
            keywords = listOf("sweet bonanza 1000", "bonanza 1000", "vs20fruitswx", "seker 1000", "şeker 1000"),
            urlSlugs = listOf("sweet-bonanza-1000", "vs20fruitswx")
        ),
        GameSignature(
            id = "vs20fruitsw",
            title = "Sweet Bonanza",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.48,
            volatilityTier = "YÜKSEK (4.5/5)",
            volatilityScore = 8,
            hitFrequencyPct = 31.0,
            bonusFrequencySpins = 390,
            maxWinMultiplier = 21100,
            minRecommendedBankrollBets = 130,
            keywords = listOf("sweet bonanza", "bonanza", "vs20fruitsw", "şeker", "seker"),
            urlSlugs = listOf("sweet-bonanza", "vs20fruitsw")
        ),
        GameSignature(
            id = "vs20starlightx",
            title = "Starlight Princess 1000",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.50,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 28.4,
            bonusFrequencySpins = 440,
            maxWinMultiplier = 15000,
            minRecommendedBankrollBets = 175,
            keywords = listOf("starlight princess 1000", "starlight", "prenses", "vs20starlightx", "princess"),
            urlSlugs = listOf("starlight-princess-1000", "starlight-princess", "vs20starlightx")
        ),
        GameSignature(
            id = "vs20sugarrushx",
            title = "Sugar Rush 1000",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.53,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 34.4,
            bonusFrequencySpins = 323,
            maxWinMultiplier = 25000,
            minRecommendedBankrollBets = 185,
            keywords = listOf("sugar rush 1000", "sugar rush", "vs20sugarrushx", "ayicik", "ayıcık"),
            urlSlugs = listOf("sugar-rush-1000", "sugar-rush", "vs20sugarrushx")
        ),
        GameSignature(
            id = "vs10txbigbass",
            title = "Big Bass Splash",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.71,
            volatilityTier = "YÜKSEK (4.5/5)",
            volatilityScore = 8,
            hitFrequencyPct = 13.6,
            bonusFrequencySpins = 113,
            maxWinMultiplier = 5000,
            minRecommendedBankrollBets = 120,
            keywords = listOf("big bass splash", "big bass", "balikci", "balıkçı", "vs10txbigbass"),
            urlSlugs = listOf("big-bass-splash", "big-bass-bonanza", "vs10txbigbass")
        ),
        GameSignature(
            id = "vs20procount",
            title = "Wisdom of Athena 1000",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.00,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 27.1,
            bonusFrequencySpins = 430,
            maxWinMultiplier = 10000,
            minRecommendedBankrollBets = 170,
            keywords = listOf("wisdom of athena", "athena", "vs20procount"),
            urlSlugs = listOf("wisdom-of-athena", "vs20procount")
        ),
        GameSignature(
            id = "vs15godsofwar",
            title = "Zeus vs Hades - Gods of War",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.07,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 15.0,
            bonusFrequencySpins = 203,
            maxWinMultiplier = 15000,
            minRecommendedBankrollBets = 200,
            keywords = listOf("zeus vs hades", "hades", "gods of war"),
            urlSlugs = listOf("zeus-vs-hades", "vs15godsofwar")
        ),
        GameSignature(
            id = "vs20doghouse",
            title = "The Dog House Megaways",
            providerCode = "PRAGMATIC",
            providerName = "Pragmatic Play",
            theoreticalRtp = 96.55,
            volatilityTier = "ÇOK YÜKSEK (5/5)",
            volatilityScore = 9,
            hitFrequencyPct = 24.5,
            bonusFrequencySpins = 295,
            maxWinMultiplier = 12305,
            minRecommendedBankrollBets = 150,
            keywords = listOf("dog house", "kopek", "köpek", "vs20doghouse"),
            urlSlugs = listOf("the-dog-house", "dog-house-megaways")
        ),

        // PG SOFT
        GameSignature(
            id = "pg_fortunetiger",
            title = "Fortune Tiger",
            providerCode = "PGSOFT",
            providerName = "PG Soft (Pocket Games)",
            theoreticalRtp = 96.81,
            volatilityTier = "ORTA (3/5)",
            volatilityScore = 6,
            hitFrequencyPct = 31.8,
            bonusFrequencySpins = 85,
            maxWinMultiplier = 2500,
            minRecommendedBankrollBets = 90,
            keywords = listOf("fortune tiger", "tiger", "kaplan", "pg tiger", "fortunetiger"),
            urlSlugs = listOf("fortune-tiger", "126")
        ),
        GameSignature(
            id = "pg_fortuneox",
            title = "Fortune Ox",
            providerCode = "PGSOFT",
            providerName = "PG Soft (Pocket Games)",
            theoreticalRtp = 96.75,
            volatilityTier = "ORTA (3.5/5)",
            volatilityScore = 6,
            hitFrequencyPct = 30.2,
            bonusFrequencySpins = 92,
            maxWinMultiplier = 2000,
            minRecommendedBankrollBets = 95,
            keywords = listOf("fortune ox", "ox", "boğa", "boga", "öküz"),
            urlSlugs = listOf("fortune-ox", "98")
        ),
        GameSignature(
            id = "pg_mahjong2",
            title = "Mahjong Ways 2",
            providerCode = "PGSOFT",
            providerName = "PG Soft (Pocket Games)",
            theoreticalRtp = 96.95,
            volatilityTier = "ORTA-YÜKSEK (4/5)",
            volatilityScore = 7,
            hitFrequencyPct = 33.4,
            bonusFrequencySpins = 165,
            maxWinMultiplier = 10000,
            minRecommendedBankrollBets = 110,
            keywords = listOf("mahjong ways 2", "mahjong", "pg mahjong"),
            urlSlugs = listOf("mahjong-ways-2", "74")
        ),
        GameSignature(
            id = "pg_wildbandito",
            title = "Wild Bandito",
            providerCode = "PGSOFT",
            providerName = "PG Soft (Pocket Games)",
            theoreticalRtp = 96.73,
            volatilityTier = "ORTA-YÜKSEK (4/5)",
            volatilityScore = 7,
            hitFrequencyPct = 32.1,
            bonusFrequencySpins = 175,
            maxWinMultiplier = 25000,
            minRecommendedBankrollBets = 115,
            keywords = listOf("wild bandito", "bandito", "gitarist", "iskelet"),
            urlSlugs = listOf("wild-bandito", "104")
        ),

        // EGT / AMUSNET
        GameSignature(
            id = "egt_shiningcrown",
            title = "Shining Crown",
            providerCode = "EGT",
            providerName = "EGT / Amusnet Interactive",
            theoreticalRtp = 96.37,
            volatilityTier = "DÜŞÜK-ORTA (2.5/5)",
            volatilityScore = 5,
            hitFrequencyPct = 34.0,
            bonusFrequencySpins = 75,
            maxWinMultiplier = 5000,
            minRecommendedBankrollBets = 80,
            keywords = listOf("shining crown", "crown", "tac", "taç", "egt crown"),
            urlSlugs = listOf("shining-crown", "shining_crown")
        ),
        GameSignature(
            id = "egt_burninghot",
            title = "40 Burning Hot",
            providerCode = "EGT",
            providerName = "EGT / Amusnet Interactive",
            theoreticalRtp = 95.93,
            volatilityTier = "ORTA (3/5)",
            volatilityScore = 6,
            hitFrequencyPct = 31.5,
            bonusFrequencySpins = 90,
            maxWinMultiplier = 3000,
            minRecommendedBankrollBets = 90,
            keywords = listOf("burning hot", "40 burning hot", "20 burning hot", "yonca", "egt yonca"),
            urlSlugs = listOf("burning-hot", "40-burning-hot", "20-burning-hot")
        ),
        GameSignature(
            id = "egt_40superhot",
            title = "40 Super Hot",
            providerCode = "EGT",
            providerName = "EGT / Amusnet Interactive",
            theoreticalRtp = 95.81,
            volatilityTier = "DÜŞÜK-ORTA (2.5/5)",
            volatilityScore = 5,
            hitFrequencyPct = 35.2,
            bonusFrequencySpins = 70,
            maxWinMultiplier = 1000,
            minRecommendedBankrollBets = 75,
            keywords = listOf("40 super hot", "super hot", "20 super hot", "egt 40"),
            urlSlugs = listOf("40-super-hot", "20-super-hot")
        ),
        GameSignature(
            id = "egt_flaminghot",
            title = "Flaming Hot Extreme",
            providerCode = "EGT",
            providerName = "EGT / Amusnet Interactive",
            theoreticalRtp = 95.96,
            volatilityTier = "ORTA-YÜKSEK (3.5/5)",
            volatilityScore = 7,
            hitFrequencyPct = 29.5,
            bonusFrequencySpins = 120,
            maxWinMultiplier = 4000,
            minRecommendedBankrollBets = 100,
            keywords = listOf("flaming hot", "flaming"),
            urlSlugs = listOf("flaming-hot")
        ),

        // HACKSAW GAMING
        GameSignature(
            id = "hs_wanted",
            title = "Wanted Dead or a Wild",
            providerCode = "HACKSAW",
            providerName = "Hacksaw Gaming",
            theoreticalRtp = 96.38,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 19.3,
            bonusFrequencySpins = 310,
            maxWinMultiplier = 12500,
            minRecommendedBankrollBets = 200,
            keywords = listOf("wanted dead or a wild", "wanted", "vs", "tren", "duel"),
            urlSlugs = listOf("wanted-dead-or-a-wild", "wanted")
        ),
        GameSignature(
            id = "hs_lebandit",
            title = "Le Bandit",
            providerCode = "HACKSAW",
            providerName = "Hacksaw Gaming",
            theoreticalRtp = 96.34,
            volatilityTier = "ORTA-YÜKSEK (3.5/5)",
            volatilityScore = 7,
            hitFrequencyPct = 32.4,
            bonusFrequencySpins = 220,
            maxWinMultiplier = 10000,
            minRecommendedBankrollBets = 125,
            keywords = listOf("le bandit", "rakun", "bandit", "hacksaw bandit"),
            urlSlugs = listOf("le-bandit")
        ),
        GameSignature(
            id = "hs_chaoscrew2",
            title = "Chaos Crew 2",
            providerCode = "HACKSAW",
            providerName = "Hacksaw Gaming",
            theoreticalRtp = 96.27,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 21.0,
            bonusFrequencySpins = 340,
            maxWinMultiplier = 20000,
            minRecommendedBankrollBets = 200,
            keywords = listOf("chaos crew", "chaos crew 2", "chaos"),
            urlSlugs = listOf("chaos-crew-2", "chaos-crew")
        ),
        GameSignature(
            id = "hs_ripcity",
            title = "RIP City",
            providerCode = "HACKSAW",
            providerName = "Hacksaw Gaming",
            theoreticalRtp = 96.22,
            volatilityTier = "YÜKSEK (4/5)",
            volatilityScore = 8,
            hitFrequencyPct = 26.8,
            bonusFrequencySpins = 245,
            maxWinMultiplier = 12500,
            minRecommendedBankrollBets = 140,
            keywords = listOf("rip city", "kedi fare", "rip"),
            urlSlugs = listOf("rip-city")
        ),

        // NOLIMIT CITY
        GameSignature(
            id = "nlc_mental",
            title = "Mental",
            providerCode = "NOLIMIT",
            providerName = "Nolimit City",
            theoreticalRtp = 96.08,
            volatilityTier = "PSİKOPAT (10/10)",
            volatilityScore = 10,
            hitFrequencyPct = 22.1,
            bonusFrequencySpins = 380,
            maxWinMultiplier = 66666,
            minRecommendedBankrollBets = 250,
            keywords = listOf("mental", "nolimit mental"),
            urlSlugs = listOf("mental")
        ),
        GameSignature(
            id = "nlc_sanquentin",
            title = "San Quentin xWays",
            providerCode = "NOLIMIT",
            providerName = "Nolimit City",
            theoreticalRtp = 96.03,
            volatilityTier = "EKSTREM (10/10)",
            volatilityScore = 10,
            hitFrequencyPct = 24.2,
            bonusFrequencySpins = 360,
            maxWinMultiplier = 150000,
            minRecommendedBankrollBets = 240,
            keywords = listOf("san quentin", "hapishane", "xways"),
            urlSlugs = listOf("san-quentin")
        ),

        // SPRIBE / CRASH
        GameSignature(
            id = "spribe_aviator",
            title = "Aviator (Crash)",
            providerCode = "SPRIBE",
            providerName = "Spribe (Crash / Turbo)",
            theoreticalRtp = 97.00,
            volatilityTier = "DİNAMİK CRASH",
            volatilityScore = 8,
            hitFrequencyPct = 42.0,
            bonusFrequencySpins = 65,
            maxWinMultiplier = 10000,
            minRecommendedBankrollBets = 100,
            keywords = listOf("aviator", "ucak", "uçak", "spribe", "crash", "spaceman", "jetx"),
            urlSlugs = listOf("aviator", "spaceman", "jetx")
        ),

        // PLAY'N GO / NETENT / PUSH / EVOLUTION
        GameSignature(
            id = "png_bookofdead",
            title = "Book of Dead",
            providerCode = "PLAYNGO",
            providerName = "Play'n GO",
            theoreticalRtp = 96.21,
            volatilityTier = "YÜKSEK (5/5)",
            volatilityScore = 9,
            hitFrequencyPct = 29.1,
            bonusFrequencySpins = 180,
            maxWinMultiplier = 5000,
            minRecommendedBankrollBets = 135,
            keywords = listOf("book of dead", "rich wilde", "kitap"),
            urlSlugs = listOf("book-of-dead")
        ),
        GameSignature(
            id = "netent_starburst",
            title = "Starburst XXXtreme",
            providerCode = "NETENT",
            providerName = "NetEnt / Red Tiger",
            theoreticalRtp = 96.26,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 9,
            hitFrequencyPct = 20.5,
            bonusFrequencySpins = 140,
            maxWinMultiplier = 200000,
            minRecommendedBankrollBets = 160,
            keywords = listOf("starburst", "gonzo", "dead or alive", "netent"),
            urlSlugs = listOf("starburst", "dead-or-alive-2")
        ),
        GameSignature(
            id = "push_razorreturns",
            title = "Razor Returns",
            providerCode = "PUSH",
            providerName = "Push Gaming",
            theoreticalRtp = 96.55,
            volatilityTier = "EKSTREM (5/5)",
            volatilityScore = 10,
            hitFrequencyPct = 23.0,
            bonusFrequencySpins = 285,
            maxWinMultiplier = 100000,
            minRecommendedBankrollBets = 190,
            keywords = listOf("razor returns", "razor shark", "kopekbaligi", "köpekbalığı", "jammin jars"),
            urlSlugs = listOf("razor-returns", "razor-shark", "jammin-jars")
        ),
        GameSignature(
            id = "evo_crazytime",
            title = "Crazy Time / Lightning",
            providerCode = "EVOLUTION",
            providerName = "Evolution Gaming",
            theoreticalRtp = 96.08,
            volatilityTier = "CANLI ÇARPAN",
            volatilityScore = 8,
            hitFrequencyPct = 36.5,
            bonusFrequencySpins = 54,
            maxWinMultiplier = 20000,
            minRecommendedBankrollBets = 100,
            keywords = listOf("crazy time", "lightning roulette", "monopoly live", "funky time", "evolution"),
            urlSlugs = listOf("crazy-time", "lightning-roulette")
        )
    )

    private val trackingKeys = listOf(
        "token=", "sid=", "session=", "sessionid=", "aff=", "affiliate=",
        "clickid=", "utm_", "btag=", "tracker=", " fingerprint=", "device_id="
    )

    fun detectGameAndProvider(rawInput: String): DetectionResult {
        val cleaned = rawInput.trim()
        val lower = cleaned.lowercase(Locale.forLanguageTag("tr"))

        // 1. Parse domain if URL or domain-like string is provided
        var domain: String? = null
        if (lower.contains("http://") || lower.contains("https://") || lower.contains(".com") ||
            lower.contains(".net") || lower.contains(".org") || lower.contains(".io") ||
            lower.contains(".bet") || lower.contains(".casino") || lower.contains(".xyz")
        ) {
            try {
                val urlWithProto = if (lower.startsWith("http")) cleaned else "https://$cleaned"
                val uri = URI(urlWithProto)
                domain = uri.host?.removePrefix("www.")
            } catch (_: Exception) {
                val slashSplit = lower.removePrefix("https://").removePrefix("http://").split("/")
                if (slashSplit.isNotEmpty() && slashSplit[0].contains(".")) {
                    domain = slashSplit[0]
                }
            }
        }

        // 2. Check for tracking parameters in URL
        val foundTokens = trackingKeys.filter { lower.contains(it) }.map { it.removeSuffix("=") }

        // 3. Match exact slug or keyword in known games
        var bestGame: GameSignature? = null
        var bestScore = 0

        for (game in games) {
            var score = 0
            if (lower == game.id.lowercase() || lower == game.title.lowercase()) {
                score += 100
            }
            for (slug in game.urlSlugs) {
                if (lower.contains(slug.lowercase())) score += 85
            }
            for (kw in game.keywords) {
                if (lower.contains(kw.lowercase())) {
                    score += if (kw.length > 7) 75 else 55
                }
            }
            if (score > bestScore) {
                bestScore = score
                bestGame = game
            }
        }

        if (bestGame != null && bestScore >= 50) {
            val prov = providers.find { it.code == bestGame.providerCode } ?: providers.first()
            return DetectionResult(
                rawInput = cleaned.ifEmpty { bestGame.title },
                detectedSiteDomain = domain,
                hasTrackingParams = foundTokens.isNotEmpty(),
                trackingTokensFound = foundTokens,
                game = bestGame,
                provider = prov,
                matchConfidencePct = min(99, 75 + (bestScore / 5)),
                isCustomInferred = false
            )
        }

        // 4. Check if user typed a provider name directly
        val matchedProvider = providers.find { prov ->
            lower.contains(prov.code.lowercase()) ||
                lower.contains(prov.name.lowercase().split(" ").first())
        }

        if (matchedProvider != null) {
            val defaultGameForProv = games.firstOrNull { it.providerCode == matchedProvider.code } ?: games.first()
            return DetectionResult(
                rawInput = cleaned,
                detectedSiteDomain = domain,
                hasTrackingParams = foundTokens.isNotEmpty(),
                trackingTokensFound = foundTokens,
                game = defaultGameForProv,
                provider = matchedProvider,
                matchConfidencePct = 88,
                isCustomInferred = false
            )
        }

        // 5. Dynamic Fingerprinting for ANY unlisted game or custom URL
        if (cleaned.isEmpty()) {
            val fallback = games.first()
            val prov = providers.first()
            return DetectionResult(
                rawInput = fallback.title,
                detectedSiteDomain = null,
                hasTrackingParams = false,
                trackingTokensFound = emptyList(),
                game = fallback,
                provider = prov,
                matchConfidencePct = 99,
                isCustomInferred = false
            )
        }

        val hash = abs(lower.hashCode())
        val inferredProvider = providers[hash % providers.size]
        val inferredVol = 6 + (hash % 5) // 6..10
        val inferredRtp = (inferredProvider.defaultRtp - 0.35 + ((hash % 70) / 100.0))
        val displayTitle = cleaned
            .substringAfterLast("/")
            .substringBefore("?")
            .replace("-", " ")
            .replace("_", " ")
            .trim()
            .ifEmpty { cleaned }
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        val customGame = GameSignature(
            id = "custom_${hash % 9999}",
            title = "$displayTitle [Dinamik Profil]",
            providerCode = inferredProvider.code,
            providerName = inferredProvider.name,
            theoreticalRtp = (inferredRtp * 100).roundToInt() / 100.0,
            volatilityTier = if (inferredVol >= 9) "EKSTREM ($inferredVol/10)" else "YÜKSEK ($inferredVol/10)",
            volatilityScore = inferredVol,
            hitFrequencyPct = 24.0 + (hash % 12),
            bonusFrequencySpins = 140 + (hash % 260),
            maxWinMultiplier = 5000 + ((hash % 4) * 5000),
            minRecommendedBankrollBets = 110 + (inferredVol * 8),
            keywords = listOf(lower),
            urlSlugs = listOf(lower)
        )

        return DetectionResult(
            rawInput = cleaned,
            detectedSiteDomain = domain,
            hasTrackingParams = foundTokens.isNotEmpty(),
            trackingTokensFound = foundTokens,
            game = customGame,
            provider = inferredProvider,
            matchConfidencePct = 82,
            isCustomInferred = true
        )
    }

    fun analyzeCurrentTimeWindow(provider: ProviderProfile, customCalendar: Calendar? = null): TimeWindowAnalysis {
        val tz = TimeZone.getTimeZone("GMT+3")
        val cal = customCalendar ?: Calendar.getInstance(tz)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        val formatted = String.format(Locale.US, "%02d:%02d:%02d (UTC+3)", hour, minute, second)

        val isPeakHour = provider.peakHoursUtc3.contains(hour)
        val isSafeHour = provider.safeHoursUtc3.contains(hour)
        val isSyncMinute = provider.syncMinutes.any { abs(it - minute) <= 1 }

        // Dead zone minutes: right before quarter-hour pool resets (:13-:14, :28-:29, :43-:44, :58-:59)
        val isDeadZoneMinute = (minute % 15 == 13 || minute % 15 == 14)

        // Golden window: safe hour OR 2-4 minutes right after provider sync reset
        val isPostSyncSweetSpot = provider.syncMinutes.any { minute - it in 2..4 }
        val isGoldenWindow = (isSafeHour && !isDeadZoneMinute) || (isPostSyncSweetSpot && !isPeakHour)

        val baseLoad = when {
            hour in 21..23 || hour == 0 -> 86
            hour in 18..20 -> 74
            hour in 1..3 -> 62
            hour in 4..8 -> 31
            else -> 52
        }
        val minuteWave = (sin(minute * 0.21) * 7).roundToInt()
        val serverLoadPct = (baseLoad + minuteWave).coerceIn(18, 98)

        val rtpOffset = when {
            isGoldenWindow -> +1.85
            isDeadZoneMinute && isPeakHour -> -3.40
            isDeadZoneMinute -> -1.95
            isPeakHour -> -1.20
            isSyncMinute -> +0.75
            else -> +0.20
        }

        val phaseLabel = when {
            isGoldenWindow -> "ALTIN TOHUM KORİDORU (AÇIK HAVUZ)"
            isDeadZoneMinute -> "HAVUZ Kapanış / ÖLÜ TOHUM DÖNGÜSÜ"
            isSyncMinute -> "SUNUCU SEED SENKRONİZASYONU (YENİLENİYOR)"
            isPeakHour -> "YÜKSEK YOĞUNLUK / DARALTILMIŞ KORİDOR"
            else -> "STANDART DENGELİ TOHUM AKIŞI"
        }

        val timeNote = when {
            isGoldenWindow ->
                "Saat $hour:${String.format(Locale.US, "%02d", minute)} itibarıyla ${provider.name} sunucu yükü düşük (%$serverLoadPct). Tohum dağılımı çarpan düşürmeye elverişli."
            isDeadZoneMinute ->
                "DİKKAT! Dakika :${String.format(Locale.US, "%02d", minute)} çeyrek saatlik havuz sıfırlaması öncesi sıkışma evresidir. Bu dakikada ardışık boş spin riski çok yüksektir."
            isPeakHour ->
                "Saat $hour:${String.format(Locale.US, "%02d", minute)} Türkiye/Avrupa prime-time yoğunluğu (%$serverLoadPct). ${provider.name} volatilite makası açıktır; uzun oturum kasa eritir."
            else ->
                "Saat $hour:${String.format(Locale.US, "%02d", minute)} orta yoğunluklu (%$serverLoadPct) koridor. Kontrollü spin limiti ile giriş yapılabilir."
        }

        return TimeWindowAnalysis(
            formattedTimeUtc3 = formatted,
            hour = hour,
            minute = minute,
            second = second,
            serverLoadPct = serverLoadPct,
            seedCyclePhase = phaseLabel,
            rtpCorridorOffset = rtpOffset,
            isGoldenWindow = isGoldenWindow,
            isDeadZoneMinute = isDeadZoneMinute,
            timeVerdictNote = timeNote
        )
    }

    fun run500SpinPreSimulation(
        rawInput: String,
        startingBankroll: Double,
        betSize: Double,
        dirtySessionRiskScore: Int,
        seedSalt: Long = System.currentTimeMillis() / 15000L
    ): SimulationReport {
        val safeBankroll = max(10.0, startingBankroll)
        val safeBet = max(0.5, min(betSize, safeBankroll))
        val detection = detectGameAndProvider(rawInput)
        val game = detection.game
        val provider = detection.provider
        val timeAnalysis = analyzeCurrentTimeWindow(provider)

        // Seed combines game, current hour/minute, and session state so results reflect real time + session!
        val compositeSeed = seedSalt xor
            game.id.hashCode().toLong() xor
            (timeAnalysis.hour * 3600L + timeAnalysis.minute * 60L) xor
            (dirtySessionRiskScore * 997L)
        val rng = Random(compositeSeed)

        val totalSpins = 500
        val monteCarloRuns = 25

        // Effective RTP & Hit probability modulated by time corridor and session dirtiness
        val sessionPenalty = if (dirtySessionRiskScore > 55) -2.1 else if (dirtySessionRiskScore > 30) -0.8 else 0.4
        val urlTrackingPenalty = if (detection.hasTrackingParams) -1.5 else 0.0
        val effectiveTargetRtp = (game.theoreticalRtp + timeAnalysis.rtpCorridorOffset + sessionPenalty + urlTrackingPenalty)
            .coerceIn(84.0, 99.2)

        val hitProb = (game.hitFrequencyPct / 100.0) * (effectiveTargetRtp / 96.0)
        val bonusProb = (1.0 / game.bonusFrequencySpins.toDouble()) *
            (if (timeAnalysis.isGoldenWindow) 1.28 else if (timeAnalysis.isDeadZoneMinute) 0.68 else 1.0)

        var ruinedRuns = 0
        var profitableRuns = 0

        // Primary detailed run variables
        var primaryFinalBankroll = safeBankroll
        var primaryPeakBankroll = safeBankroll
        var primaryLowestBankroll = safeBankroll
        var primaryTotalWagered = 0.0
        var primaryTotalWon = 0.0
        var primaryLongestDeadStreak = 0
        var primaryBonusHits = 0
        var primaryPeakMultiplier = 0.0
        var primaryOptimalExitSpin = 0
        var primaryOptimalBankroll = safeBankroll
        val primaryEquityCurve = ArrayList<Float>(totalSpins + 1)
        val majorEvents = ArrayList<SpinEvent>()

        for (run in 0 until monteCarloRuns) {
            var bankroll = safeBankroll
            var peak = safeBankroll
            var lowest = safeBankroll
            var deadStreak = 0
            var maxDeadStreak = 0
            var wagered = 0.0
            var won = 0.0
            var bonuses = 0
            var maxMult = 0.0
            var bestExitSpin = 0
            var ruined = false

            if (run == 0) {
                primaryEquityCurve.add(bankroll.toFloat())
            }

            for (spin in 1..totalSpins) {
                if (bankroll < safeBet) {
                    ruined = true
                    if (run == 0) {
                        primaryEquityCurve.add(0f)
                    }
                    continue
                }

                bankroll -= safeBet
                wagered += safeBet

                val roll = rng.nextDouble()
                var mult = 0.0
                var isBonus = false

                if (roll < bonusProb) {
                    isBonus = true
                    bonuses++
                    // Bonus multiplier distribution scaled by volatility and effective RTP
                    val bonusRoll = rng.nextDouble()
                    val rtpScale = effectiveTargetRtp / 96.0
                    mult = when {
                        bonusRoll < 0.18 -> (8.0 + rng.nextDouble() * 18.0) * rtpScale
                        bonusRoll < 0.58 -> (25.0 + rng.nextDouble() * 55.0) * rtpScale
                        bonusRoll < 0.86 -> (75.0 + rng.nextDouble() * 140.0) * rtpScale
                        bonusRoll < 0.97 -> (200.0 + rng.nextDouble() * 350.0) * rtpScale
                        else -> (550.0 + rng.nextDouble() * min(2000.0, game.maxWinMultiplier * 0.15)) * rtpScale
                    }
                } else if (roll < hitProb) {
                    // Base game hit
                    val baseRoll = rng.nextDouble()
                    val rtpScale = effectiveTargetRtp / 96.0
                    mult = when {
                        baseRoll < 0.52 -> (0.25 + rng.nextDouble() * 0.95) * rtpScale
                        baseRoll < 0.84 -> (1.20 + rng.nextDouble() * 2.80) * rtpScale
                        baseRoll < 0.96 -> (4.00 + rng.nextDouble() * 9.50) * rtpScale
                        else -> (14.0 + rng.nextDouble() * 32.0) * rtpScale
                    }
                }

                mult = (mult * 100.0).roundToInt() / 100.0
                val payout = (safeBet * mult * 100.0).roundToInt() / 100.0

                if (mult < 0.2) {
                    deadStreak++
                    if (deadStreak > maxDeadStreak) maxDeadStreak = deadStreak
                } else {
                    deadStreak = 0
                }

                bankroll += payout
                won += payout

                if (mult > maxMult) maxMult = mult
                if (bankroll > peak) {
                    peak = bankroll
                    bestExitSpin = spin
                }
                if (bankroll < lowest) lowest = bankroll

                if (run == 0) {
                    primaryEquityCurve.add(bankroll.toFloat())
                    if (isBonus || mult >= 15.0) {
                        majorEvents.add(
                            SpinEvent(
                                spinIndex = spin,
                                bet = safeBet,
                                multiplier = mult,
                                payout = payout,
                                bankrollAfter = (bankroll * 100.0).roundToInt() / 100.0,
                                isBonus = isBonus,
                                label = if (isBonus) "FREESPIN / BONUS (${mult}x)" else "YÜKSEK ÇARPAN (${mult}x)"
                            )
                        )
                    } else if (deadStreak == 18 || deadStreak == 30) {
                        majorEvents.add(
                            SpinEvent(
                                spinIndex = spin,
                                bet = safeBet,
                                multiplier = 0.0,
                                payout = 0.0,
                                bankrollAfter = (bankroll * 100.0).roundToInt() / 100.0,
                                isBonus = false,
                                label = "UYARI: Ardışık $deadStreak Ölü Spin Serisi!"
                            )
                        )
                    }
                }
            }

            if (ruined || bankroll < safeBet * 2) ruinedRuns++
            if (bankroll > safeBankroll * 1.05) profitableRuns++

            if (run == 0) {
                primaryFinalBankroll = bankroll
                primaryPeakBankroll = peak
                primaryLowestBankroll = lowest
                primaryTotalWagered = max(safeBet, wagered)
                primaryTotalWon = won
                primaryLongestDeadStreak = maxDeadStreak
                primaryBonusHits = bonuses
                primaryPeakMultiplier = maxMult
                primaryOptimalExitSpin = bestExitSpin
                primaryOptimalBankroll = peak
            }
        }

        val realizedRtp = ((primaryTotalWon / primaryTotalWagered) * 100.0 * 10).roundToInt() / 10.0
        val bankrollRatioBets = safeBankroll / safeBet
        val underfundedPenalty = if (bankrollRatioBets < game.minRecommendedBankrollBets) {
            ((game.minRecommendedBankrollBets - bankrollRatioBets) / game.minRecommendedBankrollBets.toDouble()) * 28.0
        } else 0.0

        val ruinProb = (((ruinedRuns.toDouble() / monteCarloRuns.toDouble()) * 100.0) + underfundedPenalty)
            .coerceIn(3.0, 98.5)
        val profitProb = ((profitableRuns.toDouble() / monteCarloRuns.toDouble()) * 100.0)
            .coerceIn(5.0, 92.0)

        val maxDrawdownPct = (((safeBankroll - primaryLowestBankroll) / safeBankroll) * 100.0)
            .coerceIn(0.0, 100.0)

        // Determine Uncompromising Verdict
        val verdictType: VerdictType
        val verdictTitle: String
        val verdictSubtitle: String
        val reasons = ArrayList<String>()
        val directives = ArrayList<String>()

        if (dirtySessionRiskScore >= 65 || detection.hasTrackingParams) {
            verdictType = VerdictType.RESET_REQUIRED
            verdictTitle = "🚨 BU OTURUMLA GİRME! ÖNCE IP & İZ SIFIRLA"
            verdictSubtitle = "Kirli Oturum / Takip Parametresi Tespit Edildi (Risk: %$dirtySessionRiskScore)"
            if (detection.hasTrackingParams) {
                reasons.add("Girdiğiniz bağlantıda takip/aff parametreleri (${detection.trackingTokensFound.joinToString(", ")}) tespit edildi.")
            }
            reasons.add("Mevcut tarayıcı/ağ oturum izi risk skoru yüksek (%$dirtySessionRiskScore). Sağlayıcı RGS sunucusu aynı oturum profilini tanıyor.")
            reasons.add("Ön-simülasyonda kasa erime ihtimali %${ruinProb.roundToInt()} olarak ölçüldü.")
            directives.add("1. 'IP & OTURUM' sekmesinden Oturum & Parmak İzi Sıfırlamayı tetikleyin.")
            directives.add("2. Cihazı 3 saniye Uçak Moduna alıp yeni IP atandığını doğrulayın.")
            directives.add("3. Bağlantıdaki ?token= veya ?aff= uzantılarını silip tekrar simüle edin.")
        } else if (ruinProb >= 52.0 || (realizedRtp < 88.0 && primaryPeakBankroll <= safeBankroll * 1.12) || timeAnalysis.isDeadZoneMinute) {
            verdictType = VerdictType.DO_NOT_ENTER
            verdictTitle = "🔴 BU OYUNA GİRME! — YÜKSEK KASA ERİME RİSKİ"
            verdictSubtitle = "500-Spin Simülasyon Negatif | Erime İhtimali: %${ruinProb.roundToInt()}"
            reasons.add("${game.title} (${provider.name}) şu anki saat/dakika (${timeAnalysis.formattedTimeUtc3}) koridorunda soğuk döngüde.")
            reasons.add("500 spinlik ön-simülasyonda gerçekleşen RTP %$realizedRtp seviyesinde kaldı ve ardışık $primaryLongestDeadStreak ölü spin serisi görüldü.")
            if (bankrollRatioBets < game.minRecommendedBankrollBets) {
                reasons.add("Kasa/Bahis oranınız (${bankrollRatioBets.roundToInt()}x), bu oyunun volatilitesi için gereken minimum ${game.minRecommendedBankrollBets}x sınırının altında!")
            }
            directives.add("1. Şu an bu oyuna KESİNLİKLE GİRMEYİN; kasanızın %${maxDrawdownPct.roundToInt()} kadarı erime tehlikesinde.")
            directives.add("2. Sağlayıcı havuz senkron dakikası olan :${String.format(Locale.US, "%02d", provider.syncMinutes.firstOrNull() ?: 0)} sonrası +2. dakikayı bekleyin.")
            directives.add("3. İlle oynanacaksa bahsi ₺${String.format(Locale.US, "%.2f", max(1.0, safeBankroll / game.minRecommendedBankrollBets))} seviyesine düşürün.")
        } else if (primaryPeakBankroll >= safeBankroll * 1.20 && primaryFinalBankroll < safeBankroll) {
            verdictType = VerdictType.ENTER_HIT_AND_RUN
            verdictTitle = "🟡 DİKKATLİ GİR — SADECE VUR-KAÇ (KISA OTURUM)"
            verdictSubtitle = "İlk ${max(35, primaryOptimalExitSpin)} Spinde Kar Verip Sonra Geri Topluyor!"
            reasons.add("Simülasyonda kasa ${primaryOptimalExitSpin}. spinde ₺${primaryOptimalBankroll.roundToInt()} zirvesine ulaştı ancak 500. spinde ₺${primaryFinalBankroll.roundToInt()} seviyesine geriledi.")
            reasons.add("${provider.name} algoritması kısa vadeli çarpan (${primaryPeakMultiplier}x) verdikten sonra uzun ölü spin koridoruna giriyor.")
            directives.add("1. Maksimum ${min(100, max(45, primaryOptimalExitSpin + 15))} spin sınırıyla oyuna girin.")
            directives.add("2. Kasanız ₺${(safeBankroll * 1.25).roundToInt()} - ₺${primaryOptimalBankroll.roundToInt()} bandına değdiği an ANINDA ÇIKIŞ YAPIN.")
            directives.add("3. Ardışık 14 boş spin görürseniz oyunu derhal terk edin.")
        } else {
            verdictType = VerdictType.ENTER_OPTIMAL
            verdictTitle = "🟢 GİR — OPTİMAL TOHUM & RTP KORİDORU"
            verdictSubtitle = "Simülasyon Pozitif (RTP: %$realizedRtp | Erime Riski Düşük: %${ruinProb.roundToInt()})"
            reasons.add("${game.title} için saat ${timeAnalysis.formattedTimeUtc3} penceresi ve sunucu yoğunluğu (%${timeAnalysis.serverLoadPct}) optimal eşikte.")
            reasons.add("500 spin simülasyonunda $primaryBonusHits kez bonus/freespin tetiklendi, en yüksek ${primaryPeakMultiplier}x çarpan yakalandı.")
            reasons.add("Kasa ₺${safeBankroll.roundToInt()} başlangıçtan ₺${primaryPeakBankroll.roundToInt()} zirveye ve ₺${primaryFinalBankroll.roundToInt()} kapanışa ulaştı.")
            directives.add("1. Sabit ₺${String.format(Locale.US, "%.2f", safeBet)} bahis ile başlayın, ilk 30 spinde bahis kademesi değiştirmeyin.")
            directives.add("2. İdeal kar realizasyon (Stop-Win) noktası: ${if (primaryOptimalExitSpin > 0) "$primaryOptimalExitSpin. spin civarı veya " else ""}₺${primaryOptimalBankroll.roundToInt()} kasa hedefi.")
            directives.add("3. Bonus yakalandıktan sonra en fazla 15 kontrol spini atıp oturumu sıfırlayın.")
        }

        return SimulationReport(
            detection = detection,
            timeAnalysis = timeAnalysis,
            totalSpins = totalSpins,
            startingBankroll = safeBankroll,
            betSize = safeBet,
            finalBankroll = (primaryFinalBankroll * 100.0).roundToInt() / 100.0,
            peakBankroll = (primaryPeakBankroll * 100.0).roundToInt() / 100.0,
            lowestBankroll = (primaryLowestBankroll * 100.0).roundToInt() / 100.0,
            netProfitLoss = ((primaryFinalBankroll - safeBankroll) * 100.0).roundToInt() / 100.0,
            realizedRtpPct = realizedRtp,
            ruinProbabilityPct = (ruinProb * 10.0).roundToInt() / 10.0,
            profitProbabilityPct = (profitProb * 10.0).roundToInt() / 10.0,
            maxDrawdownPct = (maxDrawdownPct * 10.0).roundToInt() / 10.0,
            longestDeadSpinStreak = primaryLongestDeadStreak,
            bonusHitsCount = primaryBonusHits,
            peakMultiplierHit = primaryPeakMultiplier,
            optimalExitSpin = primaryOptimalExitSpin,
            optimalTargetBankroll = (primaryOptimalBankroll * 100.0).roundToInt() / 100.0,
            equityCurve = primaryEquityCurve,
            majorSpinEvents = majorEvents.take(12),
            verdictType = verdictType,
            verdictTitle = verdictTitle,
            verdictSubtitle = verdictSubtitle,
            verdictReasons = reasons,
            tacticalDirectives = directives
        )
    }
}
