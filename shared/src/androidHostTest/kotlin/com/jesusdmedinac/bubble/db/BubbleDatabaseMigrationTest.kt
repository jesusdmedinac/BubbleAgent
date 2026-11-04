package com.jesusdmedinac.bubble.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Upgrades a copy of the version 1 schema snapshot that already holds user data, the way an
 * install of the previous release would be upgraded, and checks nothing is lost.
 */
class BubbleDatabaseMigrationTest {

    private lateinit var databaseFile: File
    private lateinit var driver: JdbcSqliteDriver

    @Before
    fun setUp() {
        databaseFile = File.createTempFile("bubble-v1", ".db")
        File("src/commonMain/sqldelight/databases/1.db").copyTo(databaseFile, overwrite = true)
        driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.path}")
    }

    @After
    fun tearDown() {
        driver.close()
        databaseFile.delete()
    }

    @Test
    fun `migrating from version 1 keeps existing messages and adds conversations`() {
        driver.execute(
            identifier = null,
            sql = "INSERT INTO ChatMessageEntity(id, sender, text, timestamp) VALUES ('m1', 'USER', 'Quiero tocar guitarra', 1000)",
            parameters = 0
        )

        BubbleDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = BubbleDatabase.Schema.version)

        val queries = BubbleDatabase(driver).bubbleDatabaseQueries
        val message = queries.selectAllMessages().executeAsList().single()
        assertEquals("m1", message.id)
        assertEquals("Quiero tocar guitarra", message.text)
        assertEquals("", message.conversationId)
        assertNull(message.sduiJson)

        queries.insertConversation(id = "c1", title = "Guitarra", createdAt = 2000, updatedAt = 2000)
        assertEquals(listOf("c1"), queries.selectAllConversations().executeAsList().map { it.id })
    }

    @Test
    fun `schema version counts the migration`() {
        assertEquals(2L, BubbleDatabase.Schema.version)
    }
}
