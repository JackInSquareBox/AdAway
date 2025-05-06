package org.simpleadblocker.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.simpleadblocker.R;
import org.simpleadblocker.db.converter.ListTypeConverter;
import org.simpleadblocker.db.converter.ZonedDateTimeConverter;
import org.simpleadblocker.db.dao.HostEntryDao;
import org.simpleadblocker.db.dao.HostListItemDao;
import org.simpleadblocker.db.dao.HostsSourceDao;
import org.simpleadblocker.db.entity.HostListItem;
import org.simpleadblocker.db.entity.HostsSource;
import org.simpleadblocker.db.entity.HostEntry;
import org.simpleadblocker.util.AppExecutors;

import static org.simpleadblocker.db.Migrations.MIGRATION_1_2;
import static org.simpleadblocker.db.Migrations.MIGRATION_2_3;
import static org.simpleadblocker.db.Migrations.MIGRATION_3_4;
import static org.simpleadblocker.db.Migrations.MIGRATION_4_5;
import static org.simpleadblocker.db.Migrations.MIGRATION_5_6;
import static org.simpleadblocker.db.Migrations.MIGRATION_6_7;
import static org.simpleadblocker.db.entity.HostsSource.USER_SOURCE_ID;
import static org.simpleadblocker.db.entity.HostsSource.USER_SOURCE_URL;

/**
 * This class is the application database based on Room.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
@Database(entities = {HostsSource.class, HostListItem.class, HostEntry.class}, version = 7)
@TypeConverters({ListTypeConverter.class, ZonedDateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    /**
     * The database singleton instance.
     */
    private static volatile AppDatabase instance;

    /**
     * Get the database instance.
     *
     * @param context The application context.
     * @return The database instance.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app.db"
                    ).addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            AppExecutors.getInstance().diskIO().execute(
                                    () -> AppDatabase.initialize(context, instance)
                            );
                        }
                    }).addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7
                    ).build();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the database content.
     */
    private static void initialize(Context context, AppDatabase database) {
        // Check if there is no hosts source
        HostsSourceDao hostsSourceDao = database.hostsSourceDao();
        if (!hostsSourceDao.getAll().isEmpty()) {
            return;
        }
        // User list
        HostsSource userSource = new HostsSource();
        userSource.setLabel(context.getString(R.string.hosts_user_source));
        userSource.setId(USER_SOURCE_ID);
        userSource.setUrl(USER_SOURCE_URL);
        userSource.setAllowEnabled(true);
        userSource.setRedirectEnabled(true);
        hostsSourceDao.insert(userSource);
        // AdAway official
        HostsSource source1 = new HostsSource();
        source1.setLabel(context.getString(R.string.hosts_adaway_source));
        source1.setUrl("https://adaway.org/hosts.txt");
        hostsSourceDao.insert(source1);
        // StevenBlack
        HostsSource source2 = new HostsSource();
        source2.setLabel(context.getString(R.string.hosts_stevenblack_source));
        source2.setUrl("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts");
        hostsSourceDao.insert(source2);
        // Pete Lowe
        HostsSource source3 = new HostsSource();
        source3.setLabel(context.getString(R.string.hosts_peterlowe_source));
        source3.setUrl("https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext");
        hostsSourceDao.insert(source3);
        // Custom hosts
        // Pete Lowe
        HostsSource source4 = new HostsSource();
        source4.setLabel(context.getString(R.string.hosts_custom_source));
        source4.setUrl("https://raw.githubusercontent.com/JackInSquareBox/AdAway/develop/custom_hosts/custom_hosts.txt");
        hostsSourceDao.insert(source4);
    }

    /**
     * Get the hosts source DAO.
     *
     * @return The hosts source DAO.
     */
    public abstract HostsSourceDao hostsSourceDao();

    /**
     * Get the hosts list item DAO.
     *
     * @return The hosts list item DAO.
     */
    public abstract HostListItemDao hostsListItemDao();

    /**
     * Get the hosts entry DAO.
     *
     * @return The hosts entry DAO.
     */
    public abstract HostEntryDao hostEntryDao();
}
