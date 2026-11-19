package ir.khodhesab.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.local.entity.BankEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BankRepositoryImpl].
 */
class BankRepositoryImplTest {

    private val bankDao: BankDao = mockk()
    private lateinit var repository: BankRepositoryImpl

    @Before
    fun setup() {
        repository = BankRepositoryImpl(bankDao)
    }

    @Test
    fun observeAllBanks_emitsMappedDomainBanks() = runTest {
        val bankEntities = listOf(
            BankEntity(id = 1, code = "MELLI", persianName = "بانک ملی", senderNumbers = listOf("9611"), colorHex = "#BLUE", iconKey = "icon1")
        )
        every { bankDao.getAllBanks() } returns flowOf(bankEntities)

        repository.observeAllBanks().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().code).isEqualTo("MELLI")
            awaitComplete()
        }
    }

    @Test
    fun findBankBySender_normalizesNumberAndQueriesDao() = runTest {
        val bankEntity = BankEntity(id = 1, code = "PASARGAD", persianName = "بانک پاسارگاد", senderNumbers = listOf("10009000"), colorHex = "#GOLD", iconKey = "icon2")
        coEvery { bankDao.findBankBySenderNumber("+9810009000") } returns null
        coEvery { bankDao.findBankBySenderNumber("010009000") } returns bankEntity

        val bank = repository.findBankBySender("+9810009000")
        assertThat(bank?.code).isEqualTo("PASARGAD")
    }
}
