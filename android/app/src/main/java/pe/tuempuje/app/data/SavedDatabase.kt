package pe.tuempuje.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_opportunities", primaryKeys = ["account", "id"])
data class SavedOpportunity(
    val account: String,
    val id: String,
    val ownerId: String = "",
    val title: String = "",
    val organization: String = "",
    val category: String = "",
    val location: String = "",
    val pay: String = "",
    val duration: String = "",
    val description: String = "",
    val requirements: String = "",
    val vacancies: Int = 0,
    val selectedCount: Int = 0,
) {
    fun asOpportunity(): Opportunity? = if (title.isBlank()) null else Opportunity(
        id, ownerId, title, organization, category, location, pay, duration,
        description, requirements, vacancies, selectedCount, ownerId == account,
    )

    companion object {
        fun from(account: String, opportunity: Opportunity) = SavedOpportunity(
            account, opportunity.id, opportunity.ownerId, opportunity.title,
            opportunity.organization, opportunity.category, opportunity.location,
            opportunity.pay, opportunity.duration, opportunity.description,
            opportunity.requirements, opportunity.vacancies, opportunity.selectedCount,
        )
    }
}

@Dao
interface SavedOpportunityDao {
    @Query("SELECT * FROM saved_opportunities WHERE account = :account ORDER BY rowid DESC")
    fun observe(account: String): Flow<List<SavedOpportunity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_opportunities WHERE account = :account AND id = :id)")
    suspend fun contains(account: String, id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: SavedOpportunity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun migrate(items: List<SavedOpportunity>)

    @Query("DELETE FROM saved_opportunities WHERE account = :account")
    suspend fun clearAccount(account: String)

    @Query("DELETE FROM saved_opportunities WHERE account = :account AND id = :id")
    suspend fun remove(account: String, id: String)
}

@Database(entities = [SavedOpportunity::class], version = 1, exportSchema = false)
abstract class SavedDatabase : RoomDatabase() {
    abstract fun saved(): SavedOpportunityDao

    companion object {
        @Volatile private var instance: SavedDatabase? = null

        fun get(context: Context): SavedDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, SavedDatabase::class.java, "tuempuje-guardados.db")
                .build().also { instance = it }
        }
    }
}
