package com.paranoid.mao.tsnddemo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.paranoid.mao.tsnddemo.data.AppDatabase
import com.paranoid.mao.tsnddemo.vo.Sensor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SensorDaoTest {

    lateinit var database: AppDatabase

//    @Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getContext(),
                AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAllAndGetAll() {
        val sensor1 = Sensor("test1", "00:00:00:00:00:00")
        val sensor2 = Sensor("test2", "00:00:00:00:00:01")

        val sl = listOf(sensor1, sensor2)
        database.sensorDao().insert(sensor1)
        database.sensorDao().insert(sensor1)
        database.sensorDao()
                .getAll()
                .test()
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue {
                    it.size == 2 && it.containsAll(sl)
                }
        database.sensorDao().insert(sensor1)
        database.sensorDao().insert(sensor1)
//        val list = database.sensorDao().getAllTest()
//        Log.v("T", list.toString())
//        assert(list.size == 2 && list.containsAll(sl))
    }

    @Test
    fun insertAllAndGetAllEnabled() {
        val sensor1 = Sensor("test1", "00:00:00:00:00:00", enableStatus = true)
        val sensor2 = Sensor("test2", "00:00:00:00:00:01", enableStatus = true)

        val sl = listOf(sensor1, sensor2)

        database.sensorDao().insert(sensor1, sensor2)
        database.sensorDao()
                .getEnabled()
                .test()
                .assertValue {
                    println(it.toString())
                    it.size == 2 && it.containsAll(sl)
                }
    }
}