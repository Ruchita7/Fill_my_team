package com.sample.android.fillmyteam.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.REAL;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Interface for creating playing time related table
 * @author Ruchita_Maheshwary
 *
 */
public interface PlayerMatchesColumns {


    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID="_id";

    @DataType(REAL)
    String LATITUDE="latitude";

    @DataType(REAL)
    String LONGITUDE="longitude";


    @DataType(TEXT)
    String PLAYER_EMAIL="player_email";


    @DataType(INTEGER)
    String PLAYING_TIME="playing_time";

    @DataType(TEXT)
    String PLAYING_PLACE="playing_place";


    @DataType(TEXT)
    String PLAYER_NAME="player_name";


    @DataType(TEXT)
    String PLAYING_SPORT="playing_sport";

}
