package com.android.fillmyteam.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by dgnc on 5/22/2016.
 */


@ContentProvider(authority = SportsProvider.AUTHORITY,database = SportsDatabase.class,
        packageName = "com.android.fillmyteam.provider")

public final class SportsProvider {

    private SportsProvider()    {

    }

    public static final String AUTHORITY = "com.android.fillmyteam.SportsProvider";

    public static final Uri BASE_CONTENT_URI  = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String SPORTS_INFO = "sports_info";
        String UPCOMING_MATCHES="upcoming_matches";
    }

    /**
     *
     * @param paths
     * @return
     */
    public static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();

        for (String path : paths) {
            builder.appendPath(path);
        }

        return builder.build();
    }


    @TableEndpoint(table = SportsDatabase.SPORTS_INFO)
    public static class Sports {

        @ContentUri(
                path = Path.SPORTS_INFO,
                type = "vnd.android.cursor.dir/sports_detail",
                defaultSort = SportsColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.SPORTS_INFO);


    }



    @TableEndpoint(table = SportsDatabase.UPCOMING_MATCHES)
    public static class UpcomingMatches {

        @ContentUri(
                path = Path.UPCOMING_MATCHES,
                type = "vnd.android.cursor.dir/match_detail",
                defaultSort = PlayerMatchesColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.UPCOMING_MATCHES);


    }

}
