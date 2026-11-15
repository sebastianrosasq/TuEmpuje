package pe.tuempuje.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    val noExperienceRequired: Boolean? = null,
    val workMode: String? = null,
    @ColumnInfo(defaultValue = "''") val publicBusinessName: String = "",
    @ColumnInfo(defaultValue = "''") val publicBusinessSector: String = "",
    @ColumnInfo(defaultValue = "''") val publicBusinessDescription: String = "",
) {
    fun asOpportunity(): Opportunity? = if (title.isBlank()) null else Opportunity(
        id, ownerId, title, organization, category, location, pay, duration,
        description, requirements, vacancies, selectedCount, ownerId == account, noExperienceRequired = noExperienceRequired, workMode = workMode,
        publicBusinessName = publicBusinessName, publicBusinessSector = publicBusinessSector, publicBusinessDescription = publicBusinessDescription,
    )

    companion object {
        fun from(account: String, opportunity: Opportunity) = SavedOpportunity(
            account, opportunity.id, opportunity.ownerId, opportunity.title,
            opportunity.organization, opportunity.category, opportunity.location,
            opportunity.pay, opportunity.duration, opportunity.description,
            opportunity.requirements, opportunity.vacancies, opportunity.selectedCount, opportunity.noExperienceRequired, opportunity.workMode,
            opportunity.publicBusinessName, opportunity.publicBusinessSector, opportunity.publicBusinessDescription,
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

@Database(entities = [SavedOpportunity::class], version = 3, exportSchema = false)
abstract class SavedDatabase : RoomDatabase() {
    abstract fun saved(): SavedOpportunityDao

    companion object {
        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_opportunities ADD COLUMN noExperienceRequired INTEGER")
            }
        }
        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_opportunities ADD COLUMN workMode TEXT")
                db.execSQL("ALTER TABLE saved_opportunities ADD COLUMN publicBusinessName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_opportunities ADD COLUMN publicBusinessSector TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_opportunities ADD COLUMN publicBusinessDescription TEXT NOT NULL DEFAULT ''")
            }
        }
        @Volatile private var instance: SavedDatabase? = null

        fun get(context: Context): SavedDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, SavedDatabase::class.java, "tuempuje-guardados.db")
                .addMigrations(migration1To2, migration2To3)
                .build().also { instance = it }
        }
    }
}
