package com.earth2me.essentials.storage;

import com.earth2me.essentials.IConf;
import net.ess3.api.IEssentials;
import net.ess3.api.IReload;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;


/**
 * <p>Abstract AsyncStorageObjectHolder class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public abstract class AsyncStorageObjectHolder<T extends StorageObject> implements IConf, IStorageObjectHolder<T>, IReload {
    private transient T data;
    private final transient ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final transient Class<T> clazz;
    protected final transient IEssentials ess;

    /**
     * <p>Constructor for AsyncStorageObjectHolder.</p>
     *
     * @param ess a {@link net.ess3.api.IEssentials} object.
     * @param clazz a {@link java.lang.Class} object.
     */
    public AsyncStorageObjectHolder(final IEssentials ess, final Class<T> clazz) {
        this.ess = ess;
        this.clazz = clazz;
        try {
            this.data = clazz.newInstance();
        } catch (IllegalAccessException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public T getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public void acquireReadLock() {
        rwl.readLock().lock();
    }

    /** {@inheritDoc} */
    @Override
    public void acquireWriteLock() {
        while (rwl.getReadHoldCount() > 0) {
            rwl.readLock().unlock();
        }
        rwl.writeLock().lock();
        rwl.readLock().lock();
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        unlock();
    }

    /** {@inheritDoc} */
    @Override
    public void unlock() {
        if (rwl.isWriteLockedByCurrentThread()) {
            rwl.writeLock().unlock();
            new StorageObjectDataWriter();
        }
        while (rwl.getReadHoldCount() > 0) {
            rwl.readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reloadConfig() {
        new StorageObjectDataReader();
    }

    /** {@inheritDoc} */
    @Override
    public void onReload() {
        new StorageObjectDataReader();
    }

    /**
     * <p>finishRead.</p>
     */
    public abstract void finishRead();

    /**
     * <p>finishWrite.</p>
     */
    public abstract void finishWrite();

    /**
     * <p>getStorageFile.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public abstract File getStorageFile();


    private class StorageObjectDataWriter extends AbstractDelayedYamlFileWriter {
        StorageObjectDataWriter() {
            super(ess, getStorageFile());
        }

        @Override
        public StorageObject getObject() {
            acquireReadLock();
            return getData();
        }

        @Override
        public void onFinish() {
            unlock();
            finishWrite();
        }
    }


    private class StorageObjectDataReader extends AbstractDelayedYamlFileReader<T> {
        StorageObjectDataReader() {
            super(ess, getStorageFile(), clazz);
        }

        @Override
        public void onStart() {
            rwl.writeLock().lock();
        }

        @Override
        public void onSuccess(final T object) {
            if (object != null) {
                data = object;
            }
            rwl.writeLock().unlock();
            finishRead();
        }

        @Override
        public void onException() {
            if (data == null) {
                try {
                    data = clazz.newInstance();
                } catch (IllegalAccessException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                } catch (InstantiationException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            rwl.writeLock().unlock();
        }
    }
}
