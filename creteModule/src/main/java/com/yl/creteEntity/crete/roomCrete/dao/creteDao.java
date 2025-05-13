package com.yl.creteEntity.crete.roomCrete.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.yl.creteEntity.crete.roomCrete.entity.creteEntity;

import java.util.List;
@Dao
public interface  creteDao {
    @Insert
    void insertCrete(creteEntity creteEntity);

    @Query("SELECT * FROM crete_entity")
    List<creteEntity> listCreteEntity();

    @Query("DELETE FROM crete_entity WHERE groupId = :groupId ")
    int deleteCreteEntity(String groupId);

}
