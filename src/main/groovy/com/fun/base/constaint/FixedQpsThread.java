package com.fun.base.constaint;

import com.fun.base.interfaces.MarkThread;
import com.fun.config.HttpClientConstant;
import com.fun.frame.execute.FixedQpsConcurrent;
import com.fun.frame.httpclient.GCThread;
import com.fun.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FixedQpsThread<T> extends ThreadBase {

    private static Logger logger = LoggerFactory.getLogger(FixedQpsThread.class);

    public int qps;

    public int limit;

    public boolean isTimesMode;

    public FixedQpsThread(T t, int limit, int qps, MarkThread markThread) {
        this.limit = limit;
        this.qps = qps;
        this.mark = markThread;
        this.t = t;
        isTimesMode = limit > 1000 ? true : false;
    }


    protected FixedQpsThread() {
        super();
    }

    @Override
    public void run() {
        try {
            before();
            threadmark = mark == null ? EMPTY : this.mark.mark(this);
            long s = Time.getTimeStamp();
            doing();
            long e = Time.getTimeStamp();
            long diff = e - s;
            FixedQpsConcurrent.allTimes.add(diff);
            FixedQpsConcurrent.executeTimes.getAndIncrement();
            if (diff > HttpClientConstant.MAX_ACCEPT_TIME)
                FixedQpsConcurrent.marks.add(diff + CONNECTOR + threadmark);
        } catch (Exception e) {
            logger.warn("执行任务失败！", e);
            logger.warn("执行失败对象的标记:{}", threadmark);
            FixedQpsConcurrent.errorTimes.getAndIncrement();
        } finally {
            after();
        }
    }

    @Override
    public void before() {
        GCThread.starts();
    }

    /**
     * 子类必需实现改方法,不然调用deepclone方法会报错
     *
     * @return
     */
    public abstract FixedQpsThread clone();


}
