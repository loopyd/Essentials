package com.earth2me.essentials.storage;

import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 * <p>YamlStorageReader class.</p>
 *
 * @author LoopyD
 * @version $Id: $Id
 */
public class YamlStorageReader implements IStorageReader {
    private transient static final Map<Class, Yaml> PREPARED_YAMLS = Collections.synchronizedMap(new HashMap<Class, Yaml>());
    private transient static final Map<Class, ReentrantLock> LOCKS = new HashMap<Class, ReentrantLock>();
    private transient final Reader reader;
    private transient final Plugin plugin;

    /**
     * <p>Constructor for YamlStorageReader.</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @param plugin a {@link org.bukkit.plugin.Plugin} object.
     */
    public YamlStorageReader(final Reader reader, final Plugin plugin) {
        this.reader = reader;
        this.plugin = plugin;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends StorageObject> T load(final Class<? extends T> clazz) throws ObjectLoadException {
        Yaml yaml = PREPARED_YAMLS.get(clazz);
        if (yaml == null) {
            yaml = new Yaml(prepareConstructor(clazz));
            PREPARED_YAMLS.put(clazz, yaml);
        }
        ReentrantLock lock;
        synchronized (LOCKS) {
            lock = LOCKS.get(clazz);
            if (lock == null) {
                lock = new ReentrantLock();
            }
        }
        lock.lock();
        try {
            T object = (T) yaml.load(reader);
            if (object == null) {
                object = clazz.newInstance();
            }
            return object;
        } catch (IllegalAccessException ex) {
            throw new ObjectLoadException(ex);
        } catch (InstantiationException ex) {
            throw new ObjectLoadException(ex);
        } finally {
            lock.unlock();
        }
    }

    private Constructor prepareConstructor(final Class<?> clazz) {
        final Constructor constructor = new BukkitConstructor(clazz, plugin);
        final Set<Class> classes = new HashSet<Class>();

        prepareConstructor(constructor, classes, clazz);
        return constructor;
    }

    private void prepareConstructor(final Constructor constructor, final Set<Class> classes, final Class clazz) {
        classes.add(clazz);
        final TypeDescription description = new TypeDescription(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            prepareList(field, description, classes, constructor);
            prepareMap(field, description, classes, constructor);
            if (StorageObject.class.isAssignableFrom(field.getType()) && !classes.contains(field.getType())) {
                prepareConstructor(constructor, classes, field.getType());
            }
        }
        constructor.addTypeDescription(description);
    }

    private void prepareList(final Field field, final TypeDescription description, final Set<Class> classes, final Constructor constructor) {
        final ListType listType = field.getAnnotation(ListType.class);
        if (listType != null) {
            description.putListPropertyType(field.getName(), listType.value());
            if (StorageObject.class.isAssignableFrom(listType.value()) && !classes.contains(listType.value())) {
                prepareConstructor(constructor, classes, listType.value());
            }
        }
    }

    private void prepareMap(final Field field, final TypeDescription description, final Set<Class> classes, final Constructor constructor) {
        final MapValueType mapType = field.getAnnotation(MapValueType.class);
        if (mapType != null) {
            final MapKeyType mapKeyType = field.getAnnotation(MapKeyType.class);
            description.putMapPropertyType(field.getName(), mapKeyType == null ? String.class : mapKeyType.value(), mapType.value());
            if (StorageObject.class.isAssignableFrom(mapType.value()) && !classes.contains(mapType.value())) {
                prepareConstructor(constructor, classes, mapType.value());
            }
        }
    }
}
