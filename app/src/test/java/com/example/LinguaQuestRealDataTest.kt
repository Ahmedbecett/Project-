package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CefrLevel
import com.example.data.model.PaymentMethod
import com.example.data.model.SubscriptionPlan
import com.example.data.repository.LinguaQuestRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LinguaQuestRealDataTest {

    private lateinit var context: Context
    private lateinit var repository: LinguaQuestRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("lingua_quest_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        repository = LinguaQuestRepository(context)
    }

    @Test
    fun testInitialStateHasNoMockUsersOrFakeData() {
        // Must start with 0 mock users and 0 fake payments
        assertTrue("Registered users must start empty", repository.registeredUsers.value.isEmpty())
        assertTrue("Leaderboard must start empty", repository.getLeaderboard().isEmpty())
        assertTrue("Payments must start empty", repository.paymentRequests.value.isEmpty())
        assertEquals("Daily revenue DZD must start at 0", 0, repository.dailyRevenueDzd.value)
        assertEquals("Monthly revenue DZD must start at 0", 0, repository.monthlyRevenueDzd.value)
        assertEquals("Total revenue DZD must start at 0", 0, repository.totalRevenueDzd.value)
    }

    @Test
    fun testLevelMonetizationLocksA2ThroughC2WhileA1IsFree() {
        assertTrue("Level A1 must always be free and unlocked", repository.isLevelUnlocked(CefrLevel.A1))
        assertFalse("Level A2 must be locked for free tier", repository.isLevelUnlocked(CefrLevel.A2))
        assertFalse("Level B1 must be locked for free tier", repository.isLevelUnlocked(CefrLevel.B1))
        assertFalse("Level B2 must be locked for free tier", repository.isLevelUnlocked(CefrLevel.B2))
        assertFalse("Level C1 must be locked for free tier", repository.isLevelUnlocked(CefrLevel.C1))
        assertFalse("Level C2 must be locked for free tier", repository.isLevelUnlocked(CefrLevel.C2))

        repository.setPremium(true)
        assertTrue("Level A1 unlocked after premium", repository.isLevelUnlocked(CefrLevel.A1))
        assertTrue("Level A2 unlocked after premium", repository.isLevelUnlocked(CefrLevel.A2))
        assertTrue("Level B2 unlocked after premium", repository.isLevelUnlocked(CefrLevel.B2))
        assertTrue("Level C2 unlocked after premium", repository.isLevelUnlocked(CefrLevel.C2))
    }

    @Test
    fun testRealUserRegistrationAndLeaderboardRanking() {
        // Register User 1
        repository.registerOrUpdateUser(
            userId = "user_real_1",
            name = "Ahmed Becetti",
            contact = "ahmedbecetti41@gmail.com",
            isVip = false,
            xp = 150
        )

        assertEquals(1, repository.registeredUsers.value.size)
        val u1 = repository.registeredUsers.value[0]
        assertEquals("user_real_1", u1.id)
        assertEquals("Ahmed Becetti", u1.name)
        assertEquals("ahmedbecetti41@gmail.com", u1.emailOrPhone)

        // Leaderboard now contains 1 real user
        val lb1 = repository.getLeaderboard("user_real_1")
        assertEquals(1, lb1.size)
        assertEquals("Ahmed Becetti", lb1[0].name)
        assertEquals(1, lb1[0].rank)
        assertTrue(lb1[0].isCurrentUser)

        // Register User 2 with higher XP
        repository.registerOrUpdateUser(
            userId = "user_real_2",
            name = "Sami Cherif",
            contact = "sami@example.com",
            isVip = true,
            xp = 350
        )

        assertEquals(2, repository.registeredUsers.value.size)

        // Leaderboard must sort by genuine XP: Sami (#1, 350 XP) then Ahmed (#2, 150 XP)
        val lb2 = repository.getLeaderboard("user_real_1")
        assertEquals(2, lb2.size)
        assertEquals("Sami Cherif", lb2[0].name)
        assertEquals(1, lb2[0].rank)
        assertEquals(350, lb2[0].xp)
        assertFalse(lb2[0].isCurrentUser)

        assertEquals("Ahmed Becetti", lb2[1].name)
        assertEquals(2, lb2[1].rank)
        assertEquals(150, lb2[1].xp)
        assertTrue(lb2[1].isCurrentUser)

        // Delete User 2
        repository.deleteUser("user_real_2")
        assertEquals(1, repository.registeredUsers.value.size)

        val lbAfterDelete = repository.getLeaderboard("user_real_1")
        assertEquals(1, lbAfterDelete.size)
        assertEquals("Ahmed Becetti", lbAfterDelete[0].name)
        assertEquals(1, lbAfterDelete[0].rank)
    }

    @Test
    fun testRealPaymentLedgerAndLiveRevenueCalculation() {
        // Submitting payment must keep revenue at 0 until approved
        repository.submitPayment(
            userName = "Amine Ziani",
            userContact = "0661234567",
            plan = SubscriptionPlan.MONTHLY_1,
            method = PaymentMethod.BARIDIMOB,
            transactionRef = "CCP-998811",
            receiptNotes = "Transferred 1500 DZD to 002440629137"
        )

        assertEquals(1, repository.paymentRequests.value.size)
        val req = repository.paymentRequests.value[0]
        assertEquals(0, repository.totalRevenueDzd.value)
        assertEquals(0.0, repository.totalRevenueUsdt.value, 0.01)

        // Approve payment
        repository.approvePayment(req.id)
        assertEquals(1500, repository.totalRevenueDzd.value)
        assertEquals(8.0, repository.totalRevenueUsdt.value, 0.01)
        assertEquals(1500, repository.dailyRevenueDzd.value)
        assertEquals(1500, repository.monthlyRevenueDzd.value)
    }
}
