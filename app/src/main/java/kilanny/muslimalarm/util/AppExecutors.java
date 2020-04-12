package kilanny.muslimalarm.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static AppExecutors instance;

    public static synchronized AppExecutors getInstance() {
        if (instance == null)
            instance = new AppExecutors();
        return instance;
    }

    private final Executor mCachedExecutor;

    public void executeOnCachedExecutor(Runnable runnable) {
        mCachedExecutor.execute(runnable);
    }

    private AppExecutors() {
        mCachedExecutor = Executors.newCachedThreadPool();
    }
}
