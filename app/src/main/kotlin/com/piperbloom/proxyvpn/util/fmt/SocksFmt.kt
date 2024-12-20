package com.piperbloom.proxyvpn.util.fmt

import com.piperbloom.proxyvpn.dto.EConfigType
import com.piperbloom.proxyvpn.dto.ServerConfig
import com.piperbloom.proxyvpn.dto.V2rayConfig
import com.piperbloom.proxyvpn.util.Utils

object SocksFmt {
    fun parseSocks(str: String): ServerConfig? {
        val config = ServerConfig.create(com.piperbloom.proxyvpn.dto.EConfigType.SOCKS)
        var result = str.replace(com.piperbloom.proxyvpn.dto.EConfigType.SOCKS.protocolScheme, "")
        val indexSplit = result.indexOf("#")

        if (indexSplit > 0) {
            try {
                config.remarks =
                    Utils.urlDecode(result.substring(indexSplit + 1, result.length))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            result = result.substring(0, indexSplit)
        }

        //part decode
        val indexS = result.indexOf("@")
        if (indexS > 0) {
            result = Utils.decode(result.substring(0, indexS)) + result.substring(
                indexS,
                result.length
            )
        } else {
            result = Utils.decode(result)
        }

        val legacyPattern = "^(.*):(.*)@(.+?):(\\d+?)$".toRegex()
        val match =
            legacyPattern.matchEntire(result) ?: return null

        config.outboundBean?.settings?.servers?.get(0)?.let { server ->
            server.address = match.groupValues[3].removeSurrounding("[", "]")
            server.port = match.groupValues[4].toInt()
            val socksUsersBean =
                V2rayConfig.OutboundBean.OutSettingsBean.ServersBean.SocksUsersBean()
            socksUsersBean.user = match.groupValues[1]
            socksUsersBean.pass = match.groupValues[2]
            server.users = listOf(socksUsersBean)
        }

        return config
    }

    fun toUri(config: ServerConfig): String {
        val outbound = config.getProxyOutbound() ?: return ""
        val remark = "#" + Utils.urlEncode(config.remarks)
        val pw =
            if (outbound.settings?.servers?.get(0)?.users?.get(0)?.user != null)
                "${outbound.settings?.servers?.get(0)?.users?.get(0)?.user}:${outbound.getPassword()}"
            else
                ":"
        val url = String.format(
            "%s@%s:%s",
            Utils.encode(pw),
            Utils.getIpv6Address(outbound.getServerAddress()),
            outbound.getServerPort()
        )
        return url + remark
    }
}