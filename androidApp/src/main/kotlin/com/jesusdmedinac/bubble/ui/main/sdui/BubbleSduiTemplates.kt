package com.jesusdmedinac.bubble.ui.main.sdui

import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.jsontocompose.model.ComposeAction
import com.jesusdmedinac.jsontocompose.model.ComposeDocument
import com.jesusdmedinac.jsontocompose.model.ComposeModifier
import com.jesusdmedinac.jsontocompose.model.ComposeNode
import com.jesusdmedinac.jsontocompose.model.ComposeType
import com.jesusdmedinac.jsontocompose.model.NodeProperties
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Server-Driven UI Templates for generative UI in Bubble Chat using json-to-compose.
 */
object BubbleSduiTemplates {

    const val ACTION_CONFIRM_VAULT = "confirm_vault"
    const val ACTION_DISMISS_VAULT = "dismiss_vault"

    /**
     * Builds a dynamic confirmation card rendered with json-to-compose for persisting
     * learned preferences, limits, or habits in the user's profile Vault.
     */
    fun buildConfirmVaultDocument(
        memoryId: String,
        tag: String,
        category: VaultCategory,
        memoryText: String,
        source: String = "Conversación con Bubble",
        footerDetail: String = "Detectado por Gemini Nano",
        tagColorHex: Long = 0xFF00D2D3
    ): ComposeDocument {
        val confirmEvent = "on_confirm_vault_$memoryId"
        val dismissEvent = "on_dismiss_vault_$memoryId"

        val contentColumn = ComposeNode(
            type = ComposeType.Column,
            id = "col_confirm_vault_$memoryId",
            composeModifier = ComposeModifier(
                operations = listOf(
                    ComposeModifier.Operation.FillMaxWidth,
                    ComposeModifier.Operation.Padding(12),
                ),
            ),
            properties = NodeProperties.ColumnProps(
                children = listOf(
                    ComposeNode(
                        type = ComposeType.Text,
                        id = "txt_badge_vault_$memoryId",
                        properties = NodeProperties.TextProps(
                            text = "BÓVEDA DE BIENESTAR • $tag",
                            fontSize = 11.0,
                            fontWeight = "Bold",
                            color = "#00D2D3",
                        ),
                    ),
                    ComposeNode(
                        type = ComposeType.Spacer,
                        id = "spc_1_vault_$memoryId",
                        composeModifier = ComposeModifier(
                            operations = listOf(ComposeModifier.Operation.Height(6)),
                        ),
                    ),
                    ComposeNode(
                        type = ComposeType.Text,
                        id = "txt_prompt_vault_$memoryId",
                        properties = NodeProperties.TextProps(
                            text = "¿Deseas guardar esto en tu Vault personal?",
                            fontSize = 13.0,
                            fontWeight = "Bold",
                            color = "#FFFFFF",
                        ),
                    ),
                    ComposeNode(
                        type = ComposeType.Spacer,
                        id = "spc_2_vault_$memoryId",
                        composeModifier = ComposeModifier(
                            operations = listOf(ComposeModifier.Operation.Height(4)),
                        ),
                    ),
                    ComposeNode(
                        type = ComposeType.Text,
                        id = "txt_content_vault_$memoryId",
                        properties = NodeProperties.TextProps(
                            text = "\"$memoryText\"",
                            fontSize = 12.0,
                            fontWeight = "Normal",
                            color = "#A3AAC4",
                        ),
                    ),
                    ComposeNode(
                        type = ComposeType.Spacer,
                        id = "spc_3_vault_$memoryId",
                        composeModifier = ComposeModifier(
                            operations = listOf(ComposeModifier.Operation.Height(10)),
                        ),
                    ),
                    ComposeNode(
                        type = ComposeType.Row,
                        id = "row_actions_vault_$memoryId",
                        composeModifier = ComposeModifier(
                            operations = listOf(ComposeModifier.Operation.FillMaxWidth),
                        ),
                        properties = NodeProperties.RowProps(
                            children = listOf(
                                ComposeNode(
                                    type = ComposeType.Button,
                                    id = "btn_confirm_vault_$memoryId",
                                    properties = NodeProperties.ButtonProps(
                                        onClickEventName = confirmEvent,
                                        containerColor = "#00D2D3",
                                        child = ComposeNode(
                                            type = ComposeType.Text,
                                            id = "txt_btn_confirm_vault_$memoryId",
                                            properties = NodeProperties.TextProps(
                                                text = "Recordar en Vault",
                                                fontSize = 12.0,
                                                fontWeight = "Bold",
                                                color = "#000000",
                                            ),
                                        ),
                                    ),
                                ),
                                ComposeNode(
                                    type = ComposeType.Spacer,
                                    id = "spc_btn_vault_$memoryId",
                                    composeModifier = ComposeModifier(
                                        operations = listOf(ComposeModifier.Operation.Width(8)),
                                    ),
                                ),
                                ComposeNode(
                                    type = ComposeType.Button,
                                    id = "btn_dismiss_vault_$memoryId",
                                    properties = NodeProperties.ButtonProps(
                                        onClickEventName = dismissEvent,
                                        containerColor = "#222228",
                                        child = ComposeNode(
                                            type = ComposeType.Text,
                                            id = "txt_btn_dismiss_vault_$memoryId",
                                            properties = NodeProperties.TextProps(
                                                text = "Descartar",
                                                fontSize = 12.0,
                                                fontWeight = "Normal",
                                                color = "#8E8E93",
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val rootCard = ComposeNode(
            type = ComposeType.Card,
            id = "card_confirm_vault_$memoryId",
            composeModifier = ComposeModifier(
                operations = listOf(ComposeModifier.Operation.FillMaxWidth),
            ),
            properties = NodeProperties.CardProps(
                containerColor = "#141418",
                borderColor = "#00D2D3",
                borderWidth = 1,
                cornerRadius = 14,
                child = contentColumn,
            ),
        )

        return ComposeDocument(
            actions = mapOf(
                confirmEvent to listOf(
                    ComposeAction.Custom(
                        customType = ACTION_CONFIRM_VAULT,
                        params = buildJsonObject {
                            put("id", memoryId)
                            put("tag", tag)
                            put("category", category.name)
                            put("title", memoryText)
                            put("source", source)
                            put("footerDetail", footerDetail)
                            put("tagColorHex", tagColorHex.toString())
                        },
                    ),
                ),
                dismissEvent to listOf(
                    ComposeAction.Custom(
                        customType = ACTION_DISMISS_VAULT,
                        params = buildJsonObject {
                            put("id", memoryId)
                        },
                    ),
                ),
            ),
            root = rootCard,
        )
    }

    /**
     * Builds a confirmation badge when a memory has been persisted in Vault.
     */
    fun buildMemorySavedDocument(memoryText: String): ComposeDocument {
        val rootCard = ComposeNode(
            type = ComposeType.Card,
            id = "card_saved_vault",
            composeModifier = ComposeModifier(
                operations = listOf(ComposeModifier.Operation.FillMaxWidth),
            ),
            properties = NodeProperties.CardProps(
                containerColor = "#0D2124",
                borderColor = "#00D2D3",
                borderWidth = 1,
                cornerRadius = 12,
                child = ComposeNode(
                    type = ComposeType.Column,
                    id = "col_saved_vault",
                    composeModifier = ComposeModifier(
                        operations = listOf(
                            ComposeModifier.Operation.FillMaxWidth,
                            ComposeModifier.Operation.Padding(10),
                        ),
                    ),
                    properties = NodeProperties.ColumnProps(
                        children = listOf(
                            ComposeNode(
                                type = ComposeType.Text,
                                id = "txt_saved_vault",
                                properties = NodeProperties.TextProps(
                                    text = "✓ Guardado en tu Vault: \"$memoryText\"",
                                    fontSize = 12.0,
                                    fontWeight = "SemiBold",
                                    color = "#00D2D3",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        return ComposeDocument(
            actions = emptyMap(),
            root = rootCard,
        )
    }

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun toJson(document: ComposeDocument): String = json.encodeToString(document)
}
