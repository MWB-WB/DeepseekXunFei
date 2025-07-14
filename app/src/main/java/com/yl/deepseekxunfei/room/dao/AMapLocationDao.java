package com.yl.deepseekxunfei.room.dao;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yl.deepseekxunfei.room.entity.AMapLocationEntity;

import java.util.List;

/**
 * 区县编码Dao接口
 */
@Dao
public interface AMapLocationDao {
    @Insert
    void insert(AMapLocationEntity location);

    @Insert
    void insertAll(AMapLocationEntity... locations);

    @Query("SELECT * FROM location_data WHERE name LIKE :name LIMIT 1")
    List<AMapLocationEntity> findByName(String name);

    @Query("SELECT * FROM location_data WHERE adcode = :adcode")
    List<AMapLocationEntity> findByAdcode(String adcode);

    @Query("SELECT * FROM location_data WHERE citycode = :citycode")
    List<AMapLocationEntity> findByCitycode(String citycode);
    @Query("DELETE FROM location_data ")
    void delete();
    // 重置自增序列
    @Query("DELETE FROM sqlite_sequence WHERE name='location_data'")
    void resetAutoIncrement();
}
