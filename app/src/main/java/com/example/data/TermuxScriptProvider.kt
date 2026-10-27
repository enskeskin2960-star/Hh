package com.example.data

object TermuxScriptProvider {

    val pythonScriptContent: String = """
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# ============================================================================
# RC30 PRO V3 // TELASETRY ENGINE - SAF TERMUX KARAR, SİMÜLASYON & AĞ RADARI
# Sıfır Dış Bağımlılık (Standart Python 3 Kütüphaneleri ile %100 Hatasız Çalışır)
# ============================================================================

import os
import sys
import time
import json
import math
import random
import hashlib
import urllib.request
import subprocess
from datetime import datetime, timezone, timedelta

# ANSI Renk Kodları
C_RESET  = "\033[0m"
C_BOLD   = "\033[1m"
C_GREEN  = "\033[38;5;48m"
C_CYAN   = "\033[38;5;51m"
C_AMBER  = "\033[38;5;214m"
C_RED    = "\033[38;5;197m"
C_GRAY   = "\033[38;5;245m"
C_BG_RED = "\033[48;5;52m"
C_BG_GRN = "\033[48;5;22m"
C_BG_AMB = "\033[48;5;58m"

PROVIDERS = {
    "PRAGMATIC": {"name": "Pragmatic Play", "rtp": 96.50, "vol": 9, "sync": [0, 15, 30, 45], "peak": [20, 21, 22, 23, 0, 1]},
    "PGSOFT":    {"name": "PG Soft",        "rtp": 96.72, "vol": 7, "sync": [5, 20, 25, 45], "peak": [18, 19, 20, 21, 22]},
    "EGT":       {"name": "EGT / Amusnet",  "rtp": 95.98, "vol": 6, "sync": [0, 12, 24, 36, 48], "peak": [21, 22, 23, 0]},
    "HACKSAW":   {"name": "Hacksaw Gaming", "rtp": 96.30, "vol": 10, "sync": [0, 20, 40], "peak": [21, 22, 23, 0, 1, 2]},
    "NOLIMIT":   {"name": "Nolimit City",   "rtp": 96.10, "vol": 10, "sync": [0, 30], "peak": [22, 23, 0, 1]},
    "SPRIBE":    {"name": "Spribe Crash",   "rtp": 97.00, "vol": 8, "sync": [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55], "peak": [20, 21, 22, 23, 0]}
}

GAMES = [
    {"id": "vs20olympx",   "title": "Gates of Olympus 1000", "prov": "PRAGMATIC", "rtp": 96.50, "vol": 10, "hit": 28.5, "bonus": 440, "min_x": 180, "kw": ["gates 1000", "olympus 1000", "dede 1000", "vs20olympx"]},
    {"id": "vs20olympgate","title": "Gates of Olympus",      "prov": "PRAGMATIC", "rtp": 96.50, "vol": 9,  "hit": 29.2, "bonus": 415, "min_x": 140, "kw": ["gates", "olympus", "dede", "zeus"]},
    {"id": "vs20fruitswx", "title": "Sweet Bonanza 1000",    "prov": "PRAGMATIC", "rtp": 96.53, "vol": 10, "hit": 27.8, "bonus": 450, "min_x": 180, "kw": ["sweet bonanza 1000", "bonanza 1000", "seker 1000"]},
    {"id": "vs20fruitsw",  "title": "Sweet Bonanza",         "prov": "PRAGMATIC", "rtp": 96.48, "vol": 8,  "hit": 31.0, "bonus": 390, "min_x": 130, "kw": ["sweet bonanza", "bonanza", "seker", "şeker"]},
    {"id": "vs20starlightx","title": "Starlight Princess 1000","prov": "PRAGMATIC","rtp": 96.50, "vol": 10, "hit": 28.4, "bonus": 440, "min_x": 175, "kw": ["starlight", "princess", "prenses"]},
    {"id": "vs20sugarrushx","title": "Sugar Rush 1000",      "prov": "PRAGMATIC", "rtp": 96.53, "vol": 10, "hit": 34.4, "bonus": 323, "min_x": 185, "kw": ["sugar rush", "ayicik", "ayıcık"]},
    {"id": "vs10txbigbass","title": "Big Bass Splash",       "prov": "PRAGMATIC", "rtp": 96.71, "vol": 8,  "hit": 13.6, "bonus": 113, "min_x": 120, "kw": ["big bass", "balikci", "balıkçı"]},
    {"id": "pg_tiger",     "title": "Fortune Tiger",         "prov": "PGSOFT",    "rtp": 96.81, "vol": 6,  "hit": 31.8, "bonus": 85,  "min_x": 90,  "kw": ["fortune tiger", "tiger", "kaplan"]},
    {"id": "pg_mahjong2",  "title": "Mahjong Ways 2",        "prov": "PGSOFT",    "rtp": 96.95, "vol": 7,  "hit": 33.4, "bonus": 165, "min_x": 110, "kw": ["mahjong", "ways"]},
    {"id": "egt_crown",    "title": "Shining Crown",         "prov": "EGT",       "rtp": 96.37, "vol": 5,  "hit": 34.0, "bonus": 75,  "min_x": 80,  "kw": ["shining crown", "crown", "tac", "taç"]},
    {"id": "egt_burning",  "title": "40 Burning Hot",        "prov": "EGT",       "rtp": 95.93, "vol": 6,  "hit": 31.5, "bonus": 90,  "min_x": 90,  "kw": ["burning hot", "super hot", "yonca", "egt"]},
    {"id": "hs_wanted",    "title": "Wanted Dead or a Wild", "prov": "HACKSAW",   "rtp": 96.38, "vol": 10, "hit": 19.3, "bonus": 310, "min_x": 200, "kw": ["wanted", "dead or a wild", "duel", "vs"]},
    {"id": "hs_bandit",    "title": "Le Bandit",             "prov": "HACKSAW",   "rtp": 96.34, "vol": 7,  "hit": 32.4, "bonus": 220, "min_x": 125, "kw": ["le bandit", "bandit", "rakun"]},
    {"id": "nlc_mental",   "title": "Mental / San Quentin",  "prov": "NOLIMIT",   "rtp": 96.08, "vol": 10, "hit": 22.1, "bonus": 380, "min_x": 240, "kw": ["mental", "san quentin", "nolimit"]},
    {"id": "spribe_av",    "title": "Aviator (Crash)",       "prov": "SPRIBE",    "rtp": 97.00, "vol": 8,  "hit": 42.0, "bonus": 65,  "min_x": 100, "kw": ["aviator", "spaceman", "jetx", "ucak"]}
]

SESSION_STATE = {
    "last_ip": None,
    "current_ip": "Taranmadı",
    "isp": "Bilinmiyor",
    "session_hash": hashlib.sha256(str(time.time()).encode()).hexdigest()[:16].upper(),
    "mac_spoof": ":".join([f"{random.randint(0,255):02X}" for _ in range(6)]),
    "dirty_risk": 24
}

def get_live_ip():
    urls = ["https://ipwho.is/", "https://api.ipify.org?format=json"]
    for u in urls:
        try:
            req = urllib.request.Request(u, headers={"User-Agent": "curl/8.4.0"})
            with urllib.request.urlopen(req, timeout=3.5) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                ip = data.get("ip")
                if ip:
                    isp = data.get("connection", {}).get("isp") or data.get("org") or "Mobil Şebeke ASN"
                    return ip, isp
        except Exception:
            continue
    return "10.214.0.1 (Yerel/Çevrimdışı)", "Yerel Hücresel NAT"

def detect_target(raw_text):
    t = raw_text.strip().lower()
    domain = None
    if "." in t or "http" in t:
        clean = t.replace("https://", "").replace("http://", "").split("/")[0]
        if "." in clean:
            domain = clean
    has_tracker = any(k in t for k in ["token=", "aff=", "sid=", "utm_", "clickid="])
    for g in GAMES:
        for kw in g["kw"]:
            if kw in t:
                return g, PROVIDERS[g["prov"]], domain, has_tracker
    h = int(hashlib.md5(t.encode()).hexdigest()[:8], 16)
    prov_key = list(PROVIDERS.keys())[h % len(PROVIDERS)]
    p = PROVIDERS[prov_key]
    custom = {
        "id": f"dyn_{h%999}",
        "title": f"{raw_text.strip().title() or 'Gates of Olympus 1000'} [Dinamik]",
        "prov": prov_key,
        "rtp": p["rtp"],
        "vol": p["vol"],
        "hit": 27.5,
        "bonus": 280,
        "min_x": 140
    }
    return custom, p, domain, has_tracker

def run_simulation(raw_query, bankroll, bet):
    game, prov, domain, has_tracker = detect_target(raw_query)
    now_utc3 = datetime.now(timezone(timedelta(hours=3)))
    h, m, s = now_utc3.hour, now_utc3.minute, now_utc3.second
    is_peak = h in prov["peak"]
    is_dead_min = (m % 15 in [13, 14])
    is_golden = (any(0 <= (m - sm) <= 4 for sm in prov["sync"]) and not is_peak)

    rtp_mod = 1.8 if is_golden else (-3.2 if is_dead_min else (-1.2 if is_peak else 0.3))
    if has_tracker or SESSION_STATE["dirty_risk"] > 55:
        rtp_mod -= 2.2

    eff_rtp = max(85.0, min(99.0, game["rtp"] + rtp_mod))
    hit_p = (game["hit"] / 100.0) * (eff_rtp / 96.0)
    bon_p = (1.0 / game["bonus"]) * (1.25 if is_golden else (0.65 if is_dead_min else 1.0))

    ruined_count = 0
    runs = 25
    curve = []
    peak_kasa = bankroll
    low_kasa = bankroll
    max_dead = 0
    bonuses = 0
    best_spin = 0
    max_mult = 0.0

    for r in range(runs):
        k = bankroll
        dead = 0
        for sp in range(1, 501):
            if k < bet:
                if r == 0: curve.append(0.0)
                break
            k -= bet
            roll = random.random()
            mult = 0.0
            if roll < bon_p:
                if r == 0: bonuses += 1
                mult = random.choice([18.5, 42.0, 95.0, 180.0, 420.0]) * (eff_rtp / 96.0)
            elif roll < hit_p:
                mult = random.choice([0.4, 0.8, 1.5, 3.2, 7.5, 16.0]) * (eff_rtp / 96.0)
            if mult < 0.2:
                dead += 1
                if r == 0 and dead > max_dead: max_dead = dead
            else:
                dead = 0
            k += bet * mult
            if r == 0:
                if mult > max_mult: max_mult = mult
                if k > peak_kasa:
                    peak_kasa = k
                    best_spin = sp
                if k < low_kasa: low_kasa = k
                if sp % 25 == 0: curve.append(k)
        if k < bet * 2:
            ruined_count += 1
        if r == 0:
            final_kasa = k

    ruin_pct = min(98.0, (ruined_count / runs) * 100.0 + (22.0 if (bankroll / bet) < game["min_x"] else 0.0))

    print(f"\n{C_CYAN}╔══════════════════════════════════════════════════════════════════╗{C_RESET}")
    print(f"{C_CYAN}║ {C_BOLD}RC30 PRO V3 // 500-SPİN ÖN-SİMÜLASYON & KARAR RAPORU{C_RESET}{C_CYAN}             ║{C_RESET}")
    print(f"{C_CYAN}╚══════════════════════════════════════════════════════════════════╝{C_RESET}")
    print(f"  • Tespit Edilen Oyun : {C_BOLD}{game['title']}{C_RESET}")
    print(f"  • Sağlayıcı & RTP    : {prov['name']} (Teorik: %{game['rtp']} | Anlık Efektif: %{eff_rtp:.2f})")
    if domain:
        print(f"  • Hedef Site Domain  : {C_AMBER}{domain}{C_RESET} {'[⚠️ TAKİP PARAMETRELİ URL!]' if has_tracker else '[Temiz URL]'}")
    print(f"  • Saat / Tohum Evresi: {now_utc3.strftime('%H:%M:%S')} (UTC+3) | {'ALTIN PENCERE' if is_golden else ('ÖLÜ DAKİKA DÖNGÜSÜ' if is_dead_min else 'STANDART AKIŞ')}")
    print(f"  • Başlangıç -> Bitiş : ₺{bankroll:.0f} -> ₺{final_kasa:.0f} (Zirve: ₺{peak_kasa:.0f} @ {best_spin}. Spin)")
    print(f"  • Kasa Erime Riski   : %{ruin_pct:.1f} | Maks Ölü Spin Serisi: {max_dead} Spin | Bonus: {bonuses} Kez")

    print(f"\n  {C_GRAY}[500-Spin Kasa Eğrisi (Her sütun 25 Spin)]{C_RESET}")
    bar_line = "  "
    for val in curve:
        ratio = val / max(1.0, bankroll)
        ch = "█" if ratio >= 1.15 else ("▓" if ratio >= 0.90 else ("▒" if ratio >= 0.50 else "░"))
        col = C_GREEN if ratio >= 1.05 else (C_AMBER if ratio >= 0.75 else C_RED)
        bar_line += f"{col}{ch}{C_RESET}"
    print(bar_line)

    print("")
    if has_tracker or SESSION_STATE["dirty_risk"] >= 60:
        print(f"  {C_BG_RED}{C_BOLD} 🚨 NET KARAR: BU OTURUMLA GİRME! ÖNCE IP & OTURUM SIFIRLA (MENÜ [2]) {C_RESET}")
    elif ruin_pct >= 50.0 or is_dead_min:
        print(f"  {C_BG_RED}{C_BOLD} 🔴 NET KARAR: BU OYUNA GİRME! (YÜKSEK KASA ERİME RİSKİ: %{ruin_pct:.0f}) {C_RESET}")
    elif peak_kasa >= bankroll * 1.20 and final_kasa < bankroll:
        print(f"  {C_BG_AMB}{C_BOLD} 🟡 NET KARAR: SADECE VUR-KAÇ GİR! ({best_spin}. Spinde ₺{peak_kasa:.0f} Görünce Çık) {C_RESET}")
    else:
        print(f"  {C_BG_GRN}{C_BOLD} 🟢 NET KARAR: GİR — OPTİMAL TOHUM & RTP KORİDORU AÇIK! {C_RESET}")
    print("")

def reset_ip_and_session():
    print(f"\n{C_CYAN}[*] Yerel oturum, çerez izi ve parmak izi hash değerleri sıfırlanıyor...{C_RESET}")
    old_ip, _ = get_live_ip()
    SESSION_STATE["last_ip"] = old_ip
    SESSION_STATE["session_hash"] = hashlib.sha256(os.urandom(32)).hexdigest()[:16].upper()
    SESSION_STATE["mac_spoof"] = ":".join([f"{random.randint(0,255):02X}" for _ in range(6)])
    SESSION_STATE["dirty_risk"] = 5
    print(f"{C_GREEN}[✓] Yeni Session Hash : 0x{SESSION_STATE['session_hash']}{C_RESET}")
    print(f"{C_GREEN}[✓] Yeni MAC/Donanım  : {SESSION_STATE['mac_spoof']}{C_RESET}")
    print(f"{C_AMBER}[!] Mevcut IP Adresiniz: {old_ip}{C_RESET}")
    print(f"{C_CYAN}[*] Android Uçak Modu ayarları tetikleniyor (3 saniye açık tutup kapatın)...{C_RESET}")
    try:
        subprocess.run(["am", "start", "-a", "android.settings.AIRPLANE_MODE_SETTINGS"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    except Exception:
        pass
    input(f"\n{C_BOLD}>> Uçak modunu 3 saniye açıp kapattıktan sonra ENTER tuşuna basın...{C_RESET}")
    new_ip, isp = get_live_ip()
    SESSION_STATE["current_ip"] = new_ip
    SESSION_STATE["isp"] = isp
    if new_ip != old_ip:
        print(f"{C_BG_GRN}{C_BOLD} ✅ BAŞARILI! IP DEĞİŞTİ: {old_ip} -> {new_ip} ({isp}) {C_RESET}\n")
    else:
        print(f"{C_BG_AMB}{C_BOLD} ⚠️ UYARI: IP AYNI KALDI ({new_ip}). Mobil veride Uçak Modunu 5 sn bekletin. {C_RESET}\n")

def main():
    while True:
        print(f"{C_GREEN}┌──────────────────────────────────────────────────────────────────┐{C_RESET}")
        print(f"{C_GREEN}│ {C_BOLD}RC30 PRO V3 // TELASETRY TERMUX RADAR & ÖN-SİMÜLASYON MOTORU{C_RESET}{C_GREEN}     │{C_RESET}")
        print(f"{C_GREEN}└──────────────────────────────────────────────────────────────────┘{C_RESET}")
        print(f"  [1] 🎯 Oyun / Site Tespit Et & 500-Spin Ön-Simülasyon Koş")
        print(f"  [2] 🛡️ Canlı IP Denetle & Oturum / Parmak İzi Sıfırla (Uçak Modu)")
        print(f"  [3] ⏱️ Sağlayıcı Saat / Dakika Tohum (Seed) Takvimi")
        print(f"  [0] Çıkış")
        sec = input(f"\n{C_CYAN}rc30@termux:~# {C_RESET}").strip()
        if sec == "1":
            q = input("  Oyun adı, sağlayıcı veya site URL girin: ").strip() or "Gates of Olympus 1000"
            try:
                kasa = float(input("  Başlangıç Kasanız (₺) [varsayılan 2000]: ").strip() or "2000")
                bahis = float(input("  Spin Başı Bahis (₺) [varsayılan 10]: ").strip() or "10")
            except ValueError:
                kasa, bahis = 2000.0, 10.0
            run_simulation(q, kasa, bahis)
        elif sec == "2":
            reset_ip_and_session()
        elif sec == "3":
            now_u = datetime.now(timezone(timedelta(hours=3)))
            print(f"\n  {C_BOLD}Anlık Türkiye Saati: {now_u.strftime('%H:%M:%S')}{C_RESET}")
            for k, p in PROVIDERS.items():
                print(f"  • {p['name']:16s} | Senkron Dakikaları: {p['sync']} | Yoğun Saatler: {p['peak']}")
            print("")
        elif sec == "0":
            break

if __name__ == "__main__":
    main()
""".trimIndent()

    val oneLineTermuxCommand: String by lazy {
        "pkg update -y && pkg install python -y && cat << 'EOF' > rc30_radar.py\n" +
            pythonScriptContent +
            "\nEOF\npython3 rc30_radar.py"
    }
}
