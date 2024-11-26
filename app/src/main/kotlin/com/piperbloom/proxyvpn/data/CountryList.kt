package com.piperbloom.proxyvpn.data

import com.piperbloom.proxyvpn.R

fun getCountryName(countryCode: String): String {
    val countryMap = mapOf(
        // Countries
        "AF" to "Afghanistan (AF)",
        "AL" to "Albania (AL)",
        "DZ" to "Algeria (DZ)",
        "AS" to "American Samoa (AS)",
        "AS1" to "American Samoa (AS) - 1",
        "AS2" to "American Samoa (AS) - 2",
        "AD" to "Andorra (AD)",
        "AO" to "Angola (AO)",
        "AI" to "Anguilla (AI)",
        "AQ" to "Antarctica (AQ)",
        "AG" to "Antigua and Barbuda (AG)",
        "AR" to "Argentina (AR)",
        "AM" to "Armenia (AM)",
        "AW" to "Aruba (AW)",
        "AU" to "Australia (AU)",
        "AT" to "Austria (AT)",
        "AZ" to "Azerbaijan (AZ)",
        "BS" to "Bahamas (BS)",
        "BH" to "Bahrain (BH)",
        "BD" to "Bangladesh (BD)",
        "BB" to "Barbados (BB)",
        "BY" to "Belarus (BY)",
        "BE" to "Belgium (BE)",
        "BZ" to "Belize (BZ)",
        "BJ" to "Benin (BJ)",
        "BM" to "Bermuda (BM)",
        "BT" to "Bhutan (BT)",
        "BO" to "Bolivia (BO)",
        "BA" to "Bosnia and Herzegovina (BA)",
        "BW" to "Botswana (BW)",
        "BR" to "Brazil (BR)",
        "BN" to "Brunei (BN)",
        "BG" to "Bulgaria (BG)",
        "BF" to "Burkina Faso (BF)",
        "BI" to "Burundi (BI)",
        "CV" to "Cabo Verde (CV)",
        "KH" to "Cambodia (KH)",
        "CM" to "Cameroon (CM)",
        "CA" to "Canada (CA)",
        "KY" to "Cayman Islands (KY)",
        "CF" to "Central African Republic (CF)",
        "TD" to "Chad (TD)",
        "CL" to "Chile (CL)",
        "CN" to "China (CN)",
        "CO" to "Colombia (CO)",
        "KM" to "Comoros (KM)",
        "CG" to "Congo (CG)",
        "CD" to "Congo (Democratic Republic) (CD)",
        "CR" to "Costa Rica (CR)",
        "CI" to "Côte d'Ivoire (CI)",
        "HR" to "Croatia (HR)",
        "CU" to "Cuba (CU)",
        "CY" to "Cyprus (CY)",
        "CZ" to "Czech Republic (CZ)",
        "DK" to "Denmark (DK)",
        "DJ" to "Djibouti (DJ)",
        "DM" to "Dominica (DM)",
        "DO" to "Dominican Republic (DO)",
        "EC" to "Ecuador (EC)",
        "EG" to "Egypt (EG)",
        "SV" to "El Salvador (SV)",
        "GQ" to "Equatorial Guinea (GQ)",
        "ER" to "Eritrea (ER)",
        "EE" to "Estonia (EE)",
        "SZ" to "Eswatini (SZ)",
        "ET" to "Ethiopia (ET)",
        "FJ" to "Fiji (FJ)",
        "FI" to "Finland (FI)",
        "FR" to "France (FR)",
        "GA" to "Gabon (GA)",
        "GM" to "Gambia (GM)",
        "GE" to "Georgia (GE)",
        "DE" to "Germany (DE)",
        "GH" to "Ghana (GH)",
        "GR" to "Greece (GR)",
        "GD" to "Grenada (GD)",
        "GT" to "Guatemala (GT)",
        "GN" to "Guinea (GN)",
        "GW" to "Guinea-Bissau (GW)",
        "GY" to "Guyana (GY)",
        "HT" to "Haiti (HT)",
        "HN" to "Honduras (HN)",
        "HU" to "Hungary (HU)",
        "IS" to "Iceland (IS)",
        "IN" to "India (IN)",
        "ID" to "Indonesia (ID)",
        "IR" to "Iran (IR)",
        "IQ" to "Iraq (IQ)",
        "IE" to "Ireland (IE)",
        "IL" to "Israel (IL)",
        "IT" to "Italy (IT)",
        "JM" to "Jamaica (JM)",
        "JP" to "Japan (JP)",
        "JP1" to "Japan (JP) - 1",
        "JP2" to "Japan (JP) - 2",
        "JP3" to "Japan (JP) - 3",
        "JP4" to "Japan (JP) - 4",
        "JP5" to "Japan (JP) - 5",
        "JP6" to "Japan (JP) - 6",
        "JP7" to "Japan (JP) - 7",
        "JP8" to "Japan (JP) - 8",
        "JP9" to "Japan (JP) - 9",
        "JP10" to "Japan (JP) - 10",
        "JO" to "Jordan (JO)",
        "KZ" to "Kazakhstan (KZ)",
        "KE" to "Kenya (KE)",
        "KI" to "Kiribati (KI)",
        "KP" to "Korea (North) (KP)",
        "KR" to "Korea (South) (KR)",
        "KR1" to "Korea (South) (KR) - 1",
        "KR2" to "Korea (South) (KR) - 2",
        "KR3" to "Korea (South) (KR) - 3",
        "KR4" to "Korea (South) (KR) - 4",
        "KR5" to "Korea (South) (KR) - 5",
        "KR6" to "Korea (South) (KR) - 6",
        "KR7" to "Korea (South) (KR) - 7",
        "KR8" to "Korea (South) (KR) - 8",
        "KR9" to "Korea (South) (KR) - 9",
        "KR10" to "Korea (South) (KR) - 10",
        "KW" to "Kuwait (KW)",
        "KG" to "Kyrgyzstan (KG)",
        "LA" to "Laos (LA)",
        "LV" to "Latvia (LV)",
        "LB" to "Lebanon (LB)",
        "LS" to "Lesotho (LS)",
        "LR" to "Liberia (LR)",
        "LY" to "Libya (LY)",
        "LI" to "Liechtenstein (LI)",
        "LT" to "Lithuania (LT)",
        "LU" to "Luxembourg (LU)",
        "MG" to "Madagascar (MG)",
        "MW" to "Malawi (MW)",
        "MY" to "Malaysia (MY)",
        "MV" to "Maldives (MV)",
        "ML" to "Mali (ML)",
        "MT" to "Malta (MT)",
        "MH" to "Marshall Islands (MH)",
        "MR" to "Mauritania (MR)",
        "MU" to "Mauritius (MU)",
        "MX" to "Mexico (MX)",
        "FM" to "Micronesia (FM)",
        "MD" to "Moldova (MD)",
        "MC" to "Monaco (MC)",
        "MN" to "Mongolia (MN)",
        "ME" to "Montenegro (ME)",
        "MA" to "Morocco (MA)",
        "MZ" to "Mozambique (MZ)",
        "MM" to "Myanmar (MM)",
        "NA" to "Namibia (NA)",
        "NR" to "Nauru (NR)",
        "NP" to "Nepal (NP)",
        "NL" to "Netherlands (NL)",
        "NZ" to "New Zealand (NZ)",
        "NI" to "Nicaragua (NI)",
        "NE" to "Niger (NE)",
        "NG" to "Nigeria (NG)",
        "NO" to "Norway (NO)",
        "OM" to "Oman (OM)",
        "PK" to "Pakistan (PK)",
        "PW" to "Palau (PW)",
        "PS" to "Palestine (PS)",
        "PA" to "Panama (PA)",
        "PG" to "Papua New Guinea (PG)",
        "PY" to "Paraguay (PY)",
        "PE" to "Peru (PE)",
        "PH" to "Philippines (PH)",
        "PL" to "Poland (PL)",
        "PT" to "Portugal (PT)",
        "QA" to "Qatar (QA)",
        "RO" to "Romania (RO)",
        "RU" to "Russia (RU)",
        "RW" to "Rwanda (RW)",
        "WS" to "Samoa (WS)",
        "SM" to "San Marino (SM)",
        "SA" to "Saudi Arabia (SA)",
        "SN" to "Senegal (SN)",
        "RS" to "Serbia (RS)",
        "SC" to "Seychelles (SC)",
        "SL" to "Sierra Leone (SL)",
        "SG" to "Singapore (SG)",
        "SG1" to "Singapore (SG) - 1",
        "SG2" to "Singapore (SG) - 2",
        "SG3" to "Singapore (SG) - 3",
        "SG4" to "Singapore (SG) - 4",
        "SG5" to "Singapore (SG) - 5",
        "SG6" to "Singapore (SG) - 6",
        "SG7" to "Singapore (SG) - 7",
        "SG8" to "Singapore (SG) - 8",
        "SG9" to "Singapore (SG) - 9",
        "SG10" to "Singapore (SG) - 10",
        "SK" to "Slovakia (SK)",
        "SI" to "Slovenia (SI)",
        "SO" to "Somalia (SO)",
        "ZA" to "South Africa (ZA)",
        "ES" to "Spain (ES)",
        "LK" to "Sri Lanka (LK)",
        "SD" to "Sudan (SD)",
        "SR" to "Suriname (SR)",
        "SE" to "Sweden (SE)",
        "CH" to "Switzerland (CH)",
        "SY" to "Syria (SY)",
        "TW" to "Taiwan (TW)",
        "TJ" to "Tajikistan (TJ)",
        "TZ" to "Tanzania (TZ)",
        "TH" to "Thailand (TH)",
        "TG" to "Togo (TG)",
        "TO" to "Tonga (TO)",
        "TT" to "Trinidad and Tobago (TT)",
        "TN" to "Tunisia (TN)",
        "TR" to "Turkey (TR)",
        "TM" to "Turkmenistan (TM)",
        "UG" to "Uganda (UG)",
        "UA" to "Ukraine (UA)",
        "AE" to "United Arab Emirates (AE)",
        "GB" to "United Kingdom (GB)",
        "US" to "United States (US)",
        "US1" to "United States (US) - 1",
        "US2" to "United States (US) - 2",
        "US3" to "United States (US) - 3",
        "US4" to "United States (US) - 4",
        "US5" to "United States (US) - 5",
        "US6" to "United States (US) - 6",
        "US7" to "United States (US) - 7",
        "US8" to "United States (US) - 8",
        "US9" to "United States (US) - 9",
        "US10" to "United States (US) - 10",
        "UK" to "United Kingdom (UK)",
        "UK1" to "United Kingdom (UK) - 1",
        "UK2" to "United Kingdom (UK) - 2",
        "UK3" to "United Kingdom (UK) - 3",
        "UK4" to "United Kingdom (UK) - 4",
        "UK5" to "United Kingdom (UK) - 5",
        "UK6" to "United Kingdom (UK) - 6",
        "UK7" to "United Kingdom (UK) - 7",
        "UK8" to "United Kingdom (UK) - 8",
        "UK9" to "United Kingdom (UK) - 9",
        "UK10" to "United Kingdom (UK) - 10",
        "UY" to "Uruguay (UY)",
        "UZ" to "Uzbekistan (UZ)",
        "VU" to "Vanuatu (VU)",
        "VE" to "Venezuela (VE)",
        "VN" to "Vietnam (VN)",
        "YE" to "Yemen (YE)",
        "ZM" to "Zambia (ZM)",
        "ZW" to "Zimbabwe (ZW)",

        // Territories and regions
        "AX" to "Åland Islands (AX)",
        "BQ" to "Bonaire, Sint Eustatius and Saba (BQ)",
        "BV" to "Bouvet Island (BV)",
        "CC" to "Cocos (Keeling) Islands (CC)",
        "CX" to "Christmas Island (CX)",
        "CW" to "Curaçao (CW)",
        "FK" to "Falkland Islands (Malvinas) (FK)",
        "FO" to "Faroe Islands (FO)",
        "GF" to "French Guiana (GF)",
        "PF" to "French Polynesia (PF)",
        "TF" to "French Southern Territories (TF)",
        "GU" to "Guam (GU)",
        "HM" to "Heard Island and McDonald Islands (HM)",
        "HK" to "Hong Kong (HK)",
        "HK1" to "Hong Kong (HK) - 1",
        "HK2" to "Hong Kong (HK) - 2",
        "HK3" to "Hong Kong (HK) - 3",
        "HK4" to "Hong Kong (HK) - 4",
        "HK5" to "Hong Kong (HK) - 5",
        "HK6" to "Hong Kong (HK) - 6",
        "HK7" to "Hong Kong (HK) - 7",
        "HK8" to "Hong Kong (HK) - 8",
        "HK9" to "Hong Kong (HK) - 9",
        "HK10" to "Hong Kong (HK) - 10",
        "MO" to "Macao (MO)",
        "MP" to "Northern Mariana Islands (MP)",
        "NC" to "New Caledonia (NC)",
        "NU" to "Niue (NU)",
        "NF" to "Norfolk Island (NF)",
        "PN" to "Pitcairn (PN)",
        "PR" to "Puerto Rico (PR)",
        "RE" to "Réunion (RE)",
        "SH" to "Saint Helena, Ascension and Tristan da Cunha (SH)",
        "BL" to "Saint Barthélemy (BL)",
        "MF" to "Saint Martin (French part) (MF)",
        "PM" to "Saint Pierre and Miquelon (PM)",
        "GS" to "South Georgia and the South Sandwich Islands (GS)",
        "SJ" to "Svalbard and Jan Mayen (SJ)",
        "TK" to "Tokelau (TK)",
        "TC" to "Turks and Caicos Islands (TC)",
        "UM" to "United States Minor Outlying Islands (UM)",
        "VG" to "Virgin Islands (British) (VG)",
        "VI" to "Virgin Islands (U.S.) (VI)"
    )

    // Return the full country or territory name if the code exists, else return the code itself
    return countryMap[countryCode] ?: countryCode
}


fun getCountryResourceId(countryCode: String): Int? {
    return when (countryCode) {
        "AF" -> R.raw.af
        "AL" -> R.raw.al
        "DZ" -> R.raw.dz
        "AS" -> R.raw.asflag
        "AD" -> R.raw.ad
        "AO" -> R.raw.ao
        "AI" -> R.raw.ai
        "AQ" -> R.raw.aq
        "AG" -> R.raw.ag
        "AR" -> R.raw.ar
        "AM" -> R.raw.am
        "AW" -> R.raw.aw
        "AU" -> R.raw.au
        "AT" -> R.raw.at
        "AZ" -> R.raw.az
        "BS" -> R.raw.bs
        "BH" -> R.raw.bh
        "BD" -> R.raw.bd
        "BB" -> R.raw.bb
        "BY" -> R.raw.by
        "BE" -> R.raw.be
        "BZ" -> R.raw.bz
        "BJ" -> R.raw.bj
        "BM" -> R.raw.bm
        "BT" -> R.raw.bt
        "BO" -> R.raw.bo
        "BA" -> R.raw.ba
        "BW" -> R.raw.bw
        "BR" -> R.raw.br
        "BN" -> R.raw.bn
        "BG" -> R.raw.bg
        "BF" -> R.raw.bf
        "BI" -> R.raw.bi
        "CV" -> R.raw.cv
        "KH" -> R.raw.kh
        "CM" -> R.raw.cm
        "CA" -> R.raw.ca
        "KY" -> R.raw.ky
        "CF" -> R.raw.cf
        "TD" -> R.raw.td
        "CL" -> R.raw.cl
        "CN" -> R.raw.cn
        "CO" -> R.raw.co
        "KM" -> R.raw.km
        "CG" -> R.raw.cg
        "CD" -> R.raw.cd
        "CR" -> R.raw.cr
        "CI" -> R.raw.ci
        "HR" -> R.raw.hr
        "CU" -> R.raw.cu
        "CY" -> R.raw.cy
        "CZ" -> R.raw.cz
        "DK" -> R.raw.dk
        "DJ" -> R.raw.dj
        "DM" -> R.raw.dm
        "EC" -> R.raw.ecflag
        "EG" -> R.raw.eg
        "SV" -> R.raw.sv
        "GQ" -> R.raw.gq
        "ER" -> R.raw.er
        "EE" -> R.raw.ee
        "SZ" -> R.raw.sz
        "ET" -> R.raw.et
        "FJ" -> R.raw.fj
        "FI" -> R.raw.fi
        "FR" -> R.raw.fr
        "GA" -> R.raw.ga
        "GM" -> R.raw.gm
        "GE" -> R.raw.ge
        "DE" -> R.raw.de
        "GH" -> R.raw.gh
        "GR" -> R.raw.gr
        "GD" -> R.raw.gd
        "GT" -> R.raw.gt
        "GN" -> R.raw.gn
        "GW" -> R.raw.gw
        "GY" -> R.raw.gy
        "HT" -> R.raw.ht
        "HN" -> R.raw.hn
        "HU" -> R.raw.hu
        "IS" -> R.raw.isflag
        "IN" -> R.raw.inflag
        "ID" -> R.raw.id
        "IR" -> R.raw.ir
        "IQ" -> R.raw.iq
        "IE" -> R.raw.ie
        "IL" -> R.raw.il
        "IT" -> R.raw.it
        "JM" -> R.raw.jm
        "JP" -> R.raw.jp
        "JO" -> R.raw.jo
        "KZ" -> R.raw.kz
        "KE" -> R.raw.ke
        "KI" -> R.raw.ki
        "KP" -> R.raw.kp
        "KR" -> R.raw.kr
        "KW" -> R.raw.kw
        "KG" -> R.raw.kg
        "LA" -> R.raw.la
        "LV" -> R.raw.lv
        "LB" -> R.raw.lb
        "LS" -> R.raw.ls
        "LR" -> R.raw.lr
        "LY" -> R.raw.ly
        "LI" -> R.raw.li
        "LT" -> R.raw.lt
        "LU" -> R.raw.lu
        "MG" -> R.raw.mg
        "MW" -> R.raw.mw
        "MY" -> R.raw.my
        "MV" -> R.raw.mv
        "ML" -> R.raw.ml
        "MT" -> R.raw.mt
        "MH" -> R.raw.mh
        "MR" -> R.raw.mr
        "MU" -> R.raw.mu
        "MX" -> R.raw.mx
        "FM" -> R.raw.fm
        "MD" -> R.raw.md
        "MC" -> R.raw.mc
        "MN" -> R.raw.mn
        "ME" -> R.raw.me
        "MA" -> R.raw.ma
        "MZ" -> R.raw.mz
        "MM" -> R.raw.mm
        "NA" -> R.raw.na
        "NR" -> R.raw.nr
        "NP" -> R.raw.np
        "NL" -> R.raw.nl
        "NZ" -> R.raw.nz
        "NI" -> R.raw.ni
        "NE" -> R.raw.ne
        "NG" -> R.raw.ng
        "NO" -> R.raw.no
        "OM" -> R.raw.om
        "PK" -> R.raw.pk
        "PW" -> R.raw.pw
        "PS" -> R.raw.ps
        "PA" -> R.raw.pa
        "PG" -> R.raw.pg
        "PY" -> R.raw.py
        "PE" -> R.raw.pe
        "PH" -> R.raw.ph
        "PL" -> R.raw.pl
        "PT" -> R.raw.pt
        "QA" -> R.raw.qa
        "RO" -> R.raw.ro
        "RU" -> R.raw.ru
        "RW" -> R.raw.rw
        "WS" -> R.raw.ws
        "SM" -> R.raw.sm
        "SA" -> R.raw.sa
        "SN" -> R.raw.sn
        "RS" -> R.raw.rs
        "SC" -> R.raw.sc
        "SL" -> R.raw.sl
        "SG" -> R.raw.sg
        "SK" -> R.raw.sk
        "SI" -> R.raw.si
        "SO" -> R.raw.so
        "ZA" -> R.raw.za
        "ES" -> R.raw.es
        "LK" -> R.raw.lk
        "SD" -> R.raw.sd
        "SR" -> R.raw.sr
        "SE" -> R.raw.se
        "CH" -> R.raw.ch
        "SY" -> R.raw.sy
        "TW" -> R.raw.tw
        "TJ" -> R.raw.tj
        "TZ" -> R.raw.tz
        "TH" -> R.raw.th
        "TG" -> R.raw.tg
        "TO" -> R.raw.to
        "TT" -> R.raw.tt
        "TN" -> R.raw.tn
        "TR" -> R.raw.tr
        "TM" -> R.raw.tm
        "UG" -> R.raw.ug
        "UA" -> R.raw.ua
        "AE" -> R.raw.ae
        "GB" -> R.raw.gb
        "US" -> R.raw.us
        "UY" -> R.raw.uy
        "UZ" -> R.raw.uz
        "VU" -> R.raw.vu
        "VE" -> R.raw.ve
        "VN" -> R.raw.vn
        "YE" -> R.raw.ye
        "ZM" -> R.raw.zm
        "ZW" -> R.raw.zw

            // Territories and regions
        "AX" -> R.raw.ax
        "BQ" -> R.raw.bq
        "BV" -> R.raw.bv
        "CC" -> R.raw.cc
        "CX" -> R.raw.cx
        "CW" -> R.raw.cw
        "FK" -> R.raw.fk
        "FO" -> R.raw.fo
        "GF" -> R.raw.gf
        "PF" -> R.raw.pf
        "TF" -> R.raw.tf
        "GU" -> R.raw.gu
        "HM" -> R.raw.hm
        "HK" -> R.raw.hk
        "MO" -> R.raw.mo
        "MP" -> R.raw.mp
        "NC" -> R.raw.nc
        "NU" -> R.raw.nu
        else -> null
    }
}