package com.yl.creteEntity.crete.uits;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.yl.creteEntity.crete.roomCrete.DatabaseCrete;
import com.yl.creteEntity.crete.roomCrete.entity.creteEntity;
import com.yl.creteEntity.crete.roomCrete.entity.roomDIal;

import java.util.Collections;
import java.util.List;

public class DBService extends Service{
    public DBService(){

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DBBinder();
    }
    class DBBinder extends roomDIal.Stub{
        @Override
        public List<creteEntity> listRoom() throws RemoteException {
            List<creteEntity> all = DatabaseCrete.getInstance(getApplicationContext()).creteDao().listCreteEntity();
            Log.d("seleteCrete", "Database query result: " + all.toString());
            return all;
        }
    }
}
