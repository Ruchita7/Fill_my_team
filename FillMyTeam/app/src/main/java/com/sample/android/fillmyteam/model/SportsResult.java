package com.sample.android.fillmyteam.model;

import java.util.List;

/**
 * @author Ruchita_Maheshwary
 */
public class SportsResult {
    private String name;
    private List<SportParcelable> list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SportParcelable> getList() {
        return list;
    }

    public void setList(List<SportParcelable> list) {
        this.list = list;
    }
}
