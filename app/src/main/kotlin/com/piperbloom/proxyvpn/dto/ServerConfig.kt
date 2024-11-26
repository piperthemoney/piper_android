package com.piperbloom.proxyvpn.dto

import com.piperbloom.proxyvpn.AppConfig.TAG_PROXY
import com.piperbloom.proxyvpn.AppConfig.TAG_BLOCKED
import com.piperbloom.proxyvpn.AppConfig.TAG_DIRECT
import com.piperbloom.proxyvpn.util.Utils

data class ServerConfig(
    val configVersion: Int = 3,
    val configType: com.piperbloom.proxyvpn.dto.EConfigType,
    var subscriptionId: String = "",
    val addedTime: Long = System.currentTimeMillis(),
    var remarks: String = "",
    val outboundBean: V2rayConfig.OutboundBean? = null,
    var fullConfig: V2rayConfig? = null
) {
    companion object {
        fun create(configType: com.piperbloom.proxyvpn.dto.EConfigType): ServerConfig {
            when(configType) {
                com.piperbloom.proxyvpn.dto.EConfigType.VMESS, com.piperbloom.proxyvpn.dto.EConfigType.VLESS ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                vnext = listOf(V2rayConfig.OutboundBean.OutSettingsBean.VnextBean(
                                    users = listOf(V2rayConfig.OutboundBean.OutSettingsBean.VnextBean.UsersBean())))),
                            streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean()))
                com.piperbloom.proxyvpn.dto.EConfigType.CUSTOM ->
                    return ServerConfig(configType = configType)
                com.piperbloom.proxyvpn.dto.EConfigType.SHADOWSOCKS, com.piperbloom.proxyvpn.dto.EConfigType.SOCKS, com.piperbloom.proxyvpn.dto.EConfigType.TROJAN ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                servers = listOf(V2rayConfig.OutboundBean.OutSettingsBean.ServersBean())),
                            streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean()))
                com.piperbloom.proxyvpn.dto.EConfigType.WIREGUARD ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean =  V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                secretKey = "",
                                peers = listOf(V2rayConfig.OutboundBean.OutSettingsBean.WireGuardBean())
                            )))
            }
        }
    }

    fun getProxyOutbound(): V2rayConfig.OutboundBean? {
        if (configType != com.piperbloom.proxyvpn.dto.EConfigType.CUSTOM) {
            return outboundBean
        }
        return fullConfig?.getProxyOutbound()
    }

    fun getAllOutboundTags(): MutableList<String> {
        if (configType != com.piperbloom.proxyvpn.dto.EConfigType.CUSTOM) {
            return mutableListOf(TAG_PROXY, TAG_DIRECT, TAG_BLOCKED)
        }
        fullConfig?.let { config ->
            return config.outbounds.map { it.tag }.toMutableList()
        }
        return mutableListOf()
    }

    fun getV2rayPointDomainAndPort(): String {
        val address = getProxyOutbound()?.getServerAddress().orEmpty()
        val port = getProxyOutbound()?.getServerPort()
        return Utils.getIpv6Address(address) + ":" + port
    }
}
