package com.android.fillmyteam.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

import static  net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by dgnc on 5/22/2016.
 */
public  interface  SportsColumns {

    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID="_id";

    @DataType(TEXT)
    String SPORTS_NAME="sports_name";

    @DataType(TEXT)
    String OBJECTIVE="objective";

    @DataType(TEXT)
    String PLAYERS="players";

    @DataType(TEXT)
    String RULES="rules";

    @DataType(TEXT)
    String THUMBNAIL="thumbnail";

    @DataType(TEXT)
    String POSTER_IMAGE="poster_image";

    @DataType(TEXT)
    String VIDEO_URL="video_url";

}
