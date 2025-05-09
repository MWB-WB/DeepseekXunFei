package com.yl.cretemodule.crete.roomCrete.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yl.cretemodule.crete.roomCrete.entity.creteEntity;

import java.util.List;
@Dao
public interface  creteDao {
    @Insert
    void insertCrete(creteEntity creteEntity);

    @Query("SELECT * FROM crete_entity")
    List<creteEntity> listCreteEntity();

    @Delete
    void deleteCreteEntity(creteEntity creteEntity);
}
